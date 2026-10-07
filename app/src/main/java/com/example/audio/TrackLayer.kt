package com.example.audio

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TrackBass
import com.example.ui.theme.TrackHarmonyHigh
import com.example.ui.theme.TrackHarmonyMid
import com.example.ui.theme.TrackMelody
import com.example.ui.theme.TrackPercussion

enum class TrackType(val defaultTitle: String, val color: Color) {
    MELODY("Lead Melody", TrackMelody),
    HIGH_HARMONY("High Harmony", TrackHarmonyHigh),
    MID_HARMONY("Mid Chords / Pads", TrackHarmonyMid),
    BASS("Orchestral Bass", TrackBass),
    PERCUSSION("Timpani & Percussion", TrackPercussion)
}

data class TrackLayer(
    val id: String,
    val type: TrackType,
    val name: String,
    val instrument: InstrumentType,
    val notes: List<NoteEvent> = emptyList(),
    val volume: Float = 0.85f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val pan: Float = 0.0f // -1.0 left to +1.0 right
)
