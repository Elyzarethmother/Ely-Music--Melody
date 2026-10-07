package com.example.model

enum class SectionType(val displayName: String, val tag: String) {
    INTRO("Intro", "[Intro]"),
    VERSE("Verse", "[Verse]"),
    PRE_CHORUS("Pre-Chorus", "[Pre-Chorus]"),
    CHORUS("Chorus", "[Chorus]"),
    SOLO("Orchestral Solo", "[Solo]"),
    BRIDGE("Bridge", "[Bridge]"),
    DROP("Orchestral Drop", "[Orchestral Drop]"),
    OUTRO("Outro", "[Outro]")
}

data class PieceSection(
    val type: SectionType,
    val name: String,
    val startBeat: Float,
    val lengthBeats: Float,
    val lyrics: String = "",
    val dynamicLevel: String = "mf" // p, mp, mf, f, ff
)
