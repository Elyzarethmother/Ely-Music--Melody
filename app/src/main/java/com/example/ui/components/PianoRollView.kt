package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.audio.InstrumentType
import com.example.audio.TrackLayer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GoldPrimary

@Composable
fun PianoRollView(
    tracks: List<TrackLayer>,
    totalBeats: Float,
    currentBeat: Float,
    modifier: Modifier = Modifier,
    onNoteTapped: ((pitchMidi: Int, instrument: InstrumentType) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val minMidi = 36 // C2
    val maxMidi = 84 // C6
    val totalPitches = maxMidi - minMidi
    val beatWidthPx = 36f
    val rowHeightPx = 14f

    val totalWidthDp = (totalBeats * beatWidthPx / 2.5f).dp.coerceAtLeast(600.dp)
    val totalHeightDp = (totalPitches * rowHeightPx / 2.5f).dp.coerceAtLeast(240.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(DarkSurface)
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidthDp)
                .fillMaxHeight()
                .pointerInput(tracks) {
                    detectTapGestures { offset ->
                        val beat = offset.x / beatWidthPx
                        val pitchRow = (size.height - offset.y) / (size.height / totalPitches)
                        val pitchMidi = minMidi + pitchRow.toInt()

                        // Find matching track note or default to melody
                        val hitTrack = tracks.firstOrNull { track ->
                            !track.isMuted && track.notes.any {
                                it.pitchMidi == pitchMidi &&
                                        beat >= it.startBeat && beat <= (it.startBeat + it.durationBeats)
                            }
                        }

                        val inst = hitTrack?.instrument ?: InstrumentType.VIOLIN_SOLO
                        onNoteTapped?.invoke(pitchMidi, inst)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val cellHeight = height / totalPitches
            val pixelsPerBeat = width / totalBeats

            // Draw horizontal pitch rows (alternating black and white keys background)
            for (p in 0 until totalPitches) {
                val midi = minMidi + p
                val y = height - (p + 1) * cellHeight
                val isBlackKey = when (midi % 12) {
                    1, 3, 6, 8, 10 -> true
                    else -> false
                }
                val rowColor = if (isBlackKey) Color(0xFF13101E) else Color(0xFF1A1528)

                drawRect(
                    color = rowColor,
                    topLeft = Offset(0f, y),
                    size = Size(width, cellHeight)
                )

                drawLine(
                    color = DarkSurfaceHighlight.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            // Draw vertical beat and bar grid lines
            for (b in 0..totalBeats.toInt()) {
                val x = b * pixelsPerBeat
                val isBarLine = b % 4 == 0
                val strokeW = if (isBarLine) 1.5.dp.toPx() else 0.5.dp.toPx()
                val gridColor = if (isBarLine) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)

                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = strokeW
                )
            }

            // Draw notes from each track
            val activeSolo = tracks.any { it.isSolo }
            for (track in tracks) {
                if (track.isMuted) continue
                if (activeSolo && !track.isSolo) continue

                val trackColor = track.type.color

                for (note in track.notes) {
                    val pitchRow = (note.pitchMidi - minMidi).coerceIn(0, totalPitches - 1)
                    val noteY = height - (pitchRow + 1) * cellHeight + 1f
                    val noteX = note.startBeat * pixelsPerBeat
                    val noteW = (note.durationBeats * pixelsPerBeat - 2f).coerceAtLeast(4f)
                    val noteH = cellHeight - 2f

                    drawRoundRect(
                        color = trackColor.copy(alpha = 0.9f),
                        topLeft = Offset(noteX, noteY),
                        size = Size(noteW, noteH),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Top highlight line on note
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(noteX + 2f, noteY + 1f),
                        end = Offset(noteX + noteW - 2f, noteY + 1f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Draw current playback head
            val playheadX = currentBeat * pixelsPerBeat
            drawLine(
                color = GoldPrimary,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
