package com.example.repository

import com.example.ai.GeminiOrchestrator
import com.example.audio.InstrumentType
import com.example.model.OrchestralPiece
import com.example.model.StylePresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PieceRepository(private val orchestrator: GeminiOrchestrator) {

    private val _pieces = MutableStateFlow<List<OrchestralPiece>>(emptyList())
    val pieces: StateFlow<List<OrchestralPiece>> = _pieces.asStateFlow()

    init {
        initializeSamplePieces()
    }

    private fun initializeSamplePieces() {
        val samplePieces = mutableListOf<OrchestralPiece>()

        // Preset Test Case: Modern Country Soul — 82 BPM Reference (Duet)
        val modernCountrySoulPreset = StylePresets.ALL.first { it.id == "modern_country_soul" }
        val countrySoulPiece = orchestrator.generateAlgorithmicPiece(
            seed = 8282L,
            prompt = "Modern Country Soul with fingerpicked acoustic guitar, warm electric piano, rounded bass, brushed drums, clean electric guitar fills, warm analog pads, and gentle male-female duet call and response.",
            title = "Whispers on the Porch Swing",
            lyrics = "[Verse 1 - Male Lead]\nDust on the boots and the twilight glowing low,\nTell me darling, where did the autumn go?\n\n[Verse 1 - Female Response]\nLeaves on the porch and a kettle on the fire,\nYou were always everything that I desire.\n\n[Chorus - Duet Call & Response]\n(Both) Take my hand in the slow-dance sway,\n(Male) Through every shadow at the close of day,\n(Female) Through every dawn when the shadows part,\n(Both) You hold the rhythm of a country heart.\n\n[Outro - Duet]\n(Soft acoustic strums fade into the evening breeze)",
            tempoBpm = 82,
            keySignature = "G Major",
            emotionalMood = "intimate, warm, nostalgic, tender",
            timeSignature = "4/4",
            isInstrumental = false,
            selectedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.ANALOG_PAD
            ),
            variationLabel = "82 BPM Reference"
        )
        samplePieces.add(countrySoulPiece)

        // Starter 2: Heroic Cinematic
        val piece1 = orchestrator.generateAlgorithmicPiece(
            seed = 1042L,
            prompt = "An epic cinematic battle theme with driving staccato strings, soaring French horns, and thunderous timpani drops.",
            title = "Aegis of the Sunken Citadel",
            lyrics = "[Verse 1]\nBeyond the misty mountain crest,\nThe ancient legions never rest.\n\n[Chorus]\nRaise the spears against the dark!\nIgnite the beacon, strike the spark!\n\n[Orchestral Drop]\n(Full brass fanfare and timpani thunder)\n\n[Outro]\nHonor echoes in the sky.",
            tempoBpm = 136,
            keySignature = "D Minor",
            emotionalMood = "Heroic & Triumphant",
            timeSignature = "4/4",
            isInstrumental = false,
            selectedInstruments = listOf(
                InstrumentType.STRING_SECTION,
                InstrumentType.FRENCH_HORN,
                InstrumentType.CELLO,
                InstrumentType.TIMPANI
            ),
            variationLabel = "Master Track"
        )
        samplePieces.add(piece1)

        // Starter 3: Ethereal Woodwind & Harp
        val piece2 = orchestrator.generateAlgorithmicPiece(
            seed = 2088L,
            prompt = "A nostalgic fantasy overworld theme featuring soaring flute solos, sparkling concert harp, and warm orchestral strings.",
            title = "Whispers of the Emerald Vale",
            lyrics = "[Intro]\n(Gentle harp arpeggios awaken the glade)\n\n[Verse 1]\nWhere silver willow branches bend,\nThe starlit rivers never end.\n\n[Chorus]\nBreathe the wind across the sea,\nIn timeless slumber wild and free.",
            tempoBpm = 84,
            keySignature = "C Major",
            emotionalMood = "Mystical & Ethereal",
            timeSignature = "4/4",
            isInstrumental = false,
            selectedInstruments = listOf(
                InstrumentType.FLUTE,
                InstrumentType.HARP,
                InstrumentType.STRING_SECTION,
                InstrumentType.CELLO
            ),
            variationLabel = "Master Track"
        )
        samplePieces.add(piece2)

        // Starter 4: Baroque Storm
        val piece3 = orchestrator.generateAlgorithmicPiece(
            seed = 3015L,
            prompt = "A tempestuous Baroque concerto with lightning-fast violin arpeggios and dramatic polyphonic counterpoint.",
            title = "Winter Tempest in G Minor",
            lyrics = "[Instrumental Movement]\nVirtuoso solo violin interplay with string orchestra.",
            tempoBpm = 142,
            keySignature = "G Minor",
            emotionalMood = "Dramatic & Suspenseful",
            timeSignature = "4/4",
            isInstrumental = true,
            selectedInstruments = listOf(
                InstrumentType.VIOLIN_SOLO,
                InstrumentType.STRING_SECTION,
                InstrumentType.CELLO
            ),
            variationLabel = "Master Track"
        )
        samplePieces.add(piece3)

        _pieces.value = samplePieces
    }

    fun addPiece(piece: OrchestralPiece) {
        val current = _pieces.value.toMutableList()
        current.add(0, piece)
        _pieces.value = current
    }

    fun updatePiece(updated: OrchestralPiece) {
        val current = _pieces.value.map {
            if (it.id == updated.id) updated else it
        }
        _pieces.value = current
    }

    fun toggleFavorite(pieceId: String) {
        val current = _pieces.value.map {
            if (it.id == pieceId) it.copy(isFavorite = !it.isFavorite) else it
        }
        _pieces.value = current
    }

    fun deletePiece(pieceId: String) {
        _pieces.value = _pieces.value.filter { it.id != pieceId }
    }
}
