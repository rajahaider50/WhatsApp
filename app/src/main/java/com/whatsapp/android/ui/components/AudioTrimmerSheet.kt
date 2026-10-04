package com.whatsapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.whatsapp.android.ui.theme.*

@Composable
fun AudioTrimmerSheet(
    totalDurationSeconds: Int,
    isPlaying: Boolean,
    onTogglePlay: (startMs: Long, endMs: Long) -> Unit,
    onDiscard: () -> Unit,
    onSendTrimmed: (startMs: Long, endMs: Long) -> Unit
) {
    val durationSafe = totalDurationSeconds.coerceAtLeast(1)
    var startSeconds by remember { mutableFloatStateOf(0f) }
    var endSeconds by remember { mutableFloatStateOf(durationSafe.toFloat()) }

    val trimmedDuration = (endSeconds - startSeconds).coerceAtLeast(0.5f)

    Dialog(onDismissRequest = onDiscard) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edit & Crop Audio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextLight
                    )
                    IconButton(onClick = onDiscard) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform / Preview container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WhatsAppDarkCard)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dummy visual bars highlighting cropped section
                            List(24) { i ->
                                val fraction = i / 24f
                                val isInTrim = fraction >= (startSeconds / durationSafe) && fraction <= (endSeconds / durationSafe)
                                val barColor = if (isInTrim) WhatsAppLightGreen else WhatsAppTextMuted.copy(alpha = 0.3f)
                                val barHeight = if (i % 3 == 0) 40.dp else if (i % 2 == 0) 24.dp else 16.dp

                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = String.format("Trimmed: %.1fs of %ds", trimmedDuration, durationSafe),
                            color = WhatsAppLightGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trim Start Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Start: ${String.format("%.1fs", startSeconds)}", fontSize = 12.sp, color = WhatsAppTextMuted)
                        Text("Cut from start", fontSize = 12.sp, color = WhatsAppTextMuted)
                    }
                    Slider(
                        value = startSeconds,
                        onValueChange = { newStart ->
                            if (newStart < endSeconds - 0.5f) {
                                startSeconds = newStart
                            }
                        },
                        valueRange = 0f..durationSafe.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = WhatsAppLightGreen,
                            activeTrackColor = WhatsAppTeal,
                            inactiveTrackColor = WhatsAppDarkInput
                        )
                    )
                }

                // Trim End Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("End: ${String.format("%.1fs", endSeconds)}", fontSize = 12.sp, color = WhatsAppTextMuted)
                        Text("Cut from end", fontSize = 12.sp, color = WhatsAppTextMuted)
                    }
                    Slider(
                        value = endSeconds,
                        onValueChange = { newEnd ->
                            if (newEnd > startSeconds + 0.5f) {
                                endSeconds = newEnd
                            }
                        },
                        valueRange = 0f..durationSafe.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = WhatsAppLightGreen,
                            activeTrackColor = WhatsAppTeal,
                            inactiveTrackColor = WhatsAppDarkInput
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play Preview Button
                FilledTonalButton(
                    onClick = {
                        val startMs = (startSeconds * 1000).toLong()
                        val endMs = (endSeconds * 1000).toLong()
                        onTogglePlay(startMs, endMs)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = WhatsAppDarkCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = WhatsAppLightGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "Pause Preview" else "Play Trimmed Audio",
                        color = WhatsAppTextLight
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Discard / Send Edited
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = WhatsAppRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discard", color = WhatsAppRed)
                    }

                    Button(
                        onClick = {
                            val startMs = (startSeconds * 1000).toLong()
                            val endMs = (endSeconds * 1000).toLong()
                            onSendTrimmed(startMs, endMs)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Edited", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
