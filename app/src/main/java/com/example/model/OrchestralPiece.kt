package com.example.model

import com.example.audio.TrackLayer

data class OrchestralPiece(
    val id: String,
    val title: String,
    val prompt: String,
    val styleDescription: String,
    val tempoBpm: Int = 112,
    val keySignature: String = "D Minor",
    val emotionalMood: String = "Heroic & Triumphant",
    val timeSignature: String = "4/4",
    val instrumentationTags: List<String> = emptyList(),
    val lyrics: String = "",
    val isInstrumental: Boolean = false,
    val sections: List<PieceSection> = emptyList(),
    val tracks: List<TrackLayer> = emptyList(),
    val totalBeats: Float = 32f,
    val chordProgression: List<String> = listOf("Dm (i)", "B♭ (♭VI)", "F (♭III)", "A⁷ (V⁷)", "Dm (i)", "Gm⁶ (iv⁶)", "A⁷ˢᵘˢ⁴", "Dm (i)"),
    val harmonicStyleName: String = "Romantic Mediant Swell",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val variationLabel: String = "Variation A",
    val accentColorHex: Long = 0xFFF5B74F
) {
    val durationSeconds: Float
        get() = (totalBeats * 60f) / tempoBpm.coerceAtLeast(30)

    val durationFormatted: String
        get() {
            val totalSec = durationSeconds.toInt()
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
