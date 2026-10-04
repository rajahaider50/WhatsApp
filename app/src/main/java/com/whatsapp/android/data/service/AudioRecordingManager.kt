package com.whatsapp.android.data.service

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

class AudioRecordingManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    fun startRecording(): Result<Unit> {
        return try {
            stopPreview()
            val outputDir = File(context.cacheDir, "voice_notes")
            if (!outputDir.exists()) outputDir.mkdirs()

            val file = File(outputDir, "rec_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true
            _recordingDurationSeconds.value = 0
            _amplitudes.value = emptyList()

            recordingJob = CoroutineScope(Dispatchers.Main).launch {
                var seconds = 0
                val sampleList = mutableListOf<Float>()
                while (isActive && _isRecording.value) {
                    delay(100)
                    val amp = try {
                        val max = recorder?.maxAmplitude ?: 0
                        (max / 32767f).coerceIn(0.05f, 1f)
                    } catch (e: Exception) {
                        0.1f
                    }
                    sampleList.add(amp)
                    if (sampleList.size > 50) sampleList.removeAt(0)
                    _amplitudes.value = sampleList.toList()

                    if (sampleList.size % 10 == 0) {
                        seconds++
                        _recordingDurationSeconds.value = seconds
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _isRecording.value = false
            Result.failure(e)
        }
    }

    fun stopRecording(): File? {
        recordingJob?.cancel()
        recordingJob = null
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            _isRecording.value = false
            currentOutputFile
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            _isRecording.value = false
            currentOutputFile
        }
    }

    fun cancelRecording() {
        stopRecording()
        currentOutputFile?.delete()
        currentOutputFile = null
        _amplitudes.value = emptyList()
        _recordingDurationSeconds.value = 0
    }

    fun startPreview(file: File, onComplete: () -> Unit = {}) {
        stopPreview()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlayingPreview.value = false
                    onComplete()
                }
                start()
            }
            _isPlayingPreview.value = true
        } catch (e: Exception) {
            _isPlayingPreview.value = false
        }
    }

    fun stopPreview() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        _isPlayingPreview.value = false
    }

    // Audio Cropping / Trimming using MediaExtractor and MediaMuxer
    suspend fun cropAudio(
        inputFile: File,
        startMs: Long,
        endMs: Long
    ): Result<File> = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        try {
            val outputDir = File(context.cacheDir, "voice_notes_cropped")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "crop_${System.currentTimeMillis()}.m4a")

            extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)

            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return@withContext Result.failure(Exception("No audio track found in file"))
            }

            extractor.selectTrack(audioTrackIndex)
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                1024 * 64
            }
            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                val sampleTime = extractor.sampleTime // in microseconds
                if (sampleTime > endMs * 1000L) break

                bufferInfo.presentationTimeUs = sampleTime - (startMs * 1000L)
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            muxer.stop()
            Result.success(outputFile)
        } catch (e: Exception) {
            // Fallback: If device doesn't support muxing, return original file
            Result.success(inputFile)
        } finally {
            try { extractor?.release() } catch (ignored: Exception) {}
            try { muxer?.release() } catch (ignored: Exception) {}
        }
    }
}
