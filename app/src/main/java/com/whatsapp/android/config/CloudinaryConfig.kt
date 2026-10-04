package com.whatsapp.android.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object CloudinaryConfig {
    const val CLOUD_NAME = "dtpuqzq0e"
    const val API_KEY = "383252124344779"
    const val UPLOAD_PRESET = "whatsapp_uploads"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        file: File,
        folder: String = "whatsapp_media",
        resourceType: String = "auto"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mediaType = when {
                file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) -> "image/jpeg"
                file.name.endsWith(".png", true) -> "image/png"
                file.name.endsWith(".mp4", true) -> "video/mp4"
                file.name.endsWith(".m4a", true) || file.name.endsWith(".aac", true) || file.name.endsWith(".mp3", true) -> "audio/mp4"
                file.name.endsWith(".pdf", true) -> "application/pdf"
                else -> "application/octet-stream"
            }.toMediaTypeOrNull()

            val fileBody = file.asRequestBody(mediaType)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart("file", file.name, fileBody)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotEmpty()) {
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary secure_url missing in response"))
                }
            } else {
                Result.failure(Exception("Upload failed with code ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBytes(
        bytes: ByteArray,
        filename: String,
        mimeType: String,
        folder: String = "whatsapp_media"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart("file", filename, body)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotEmpty()) {
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary secure_url missing"))
                }
            } else {
                Result.failure(Exception("Upload failed: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
