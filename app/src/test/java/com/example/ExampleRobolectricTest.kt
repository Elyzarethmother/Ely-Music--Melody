package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiOrchestrator
import com.example.audio.MusicalScales
import com.example.model.HarmonicStyle
import com.example.model.LyricUtils
import com.example.model.StylePresets
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Symphonia", appName)
    }

    @Test
    fun `musical scales and midi conversion work accurately`() {
        val rootC = MusicalScales.parseRootMidi("C Major")
        assertEquals(60, rootC)

        val rootD = MusicalScales.parseRootMidi("D Minor")
        assertEquals(62, rootD)

        val scalePitches = MusicalScales.getScalePitches(60, MusicalScales.MAJOR_INTERVALS, octaves = 1)
        assertEquals(listOf(60, 62, 64, 65, 67, 69, 71), scalePitches)
    }

    @Test
    fun `lyric syllable counter operates correctly`() {
        val syl1 = LyricUtils.countSyllablesInWord("symphony")
        assertEquals(3, syl1)

        val syl2 = LyricUtils.countSyllablesInWord("orchestra")
        assertEquals(3, syl2)

        val lineCount = LyricUtils.countSyllablesInLine("Beyond the misty mountain crest")
        assertEquals(8, lineCount)
    }

    @Test
    fun `algorithmic orchestral composer generates multi-track piece`() {
        val orchestrator = GeminiOrchestrator()
        val piece = orchestrator.generateAlgorithmicPiece(
            seed = 1234L,
            prompt = "An epic cinematic battle theme",
            title = "Test Overture"
        )

        assertNotNull(piece)
        assertEquals("Test Overture", piece.title)
        assertTrue(piece.tracks.isNotEmpty())
        assertEquals(5, piece.tracks.size) // Melody, High Harmony, Mid Harmony, Bass, Percussion
        assertTrue(piece.tracks.first().notes.isNotEmpty())
    }

    @Test
    fun `sophisticated classical and romantic harmony layers generate counterpoint and progressions`() = runBlocking {
        val orchestrator = GeminiOrchestrator()
        val piece = orchestrator.generateAlgorithmicPiece(
            seed = 5678L,
            prompt = "Symphonic movement in D minor",
            title = "Nocturne"
        )

        val (tracks, chords) = orchestrator.generateSophisticatedHarmonies(
            piece = piece,
            style = HarmonicStyle.ROMANTIC_BRAHMS,
            useContraryMotion = true,
            useSuspensions = true,
            useHarpArpeggios = true
        )

        assertEquals(5, tracks.size)
        assertEquals(8, chords.size) // 8 bars
        assertTrue(chords.first().contains("i (Tonic)"))
    }

    @Test
    fun `validate Modern Country Soul 82 BPM reference preset test case`() {
        val preset = StylePresets.ALL.find { it.id == "modern_country_soul" }
        assertNotNull(preset)
        requireNotNull(preset)

        assertEquals("Modern Country Soul", preset.name)
        assertEquals(82, preset.defaultBpm)
        assertEquals(Pair(78, 92), preset.bpmRange)
        assertEquals("slow-dance sway", preset.groove)
        assertEquals("intimate, warm, nostalgic, tender", preset.mood)
        assertTrue(preset.isDuet)
        assertEquals("natural male-female duet with gentle call and response", preset.vocalCharacter)
        assertEquals("subtle", preset.production.tapeDelay)
        assertEquals("soft plate", preset.production.reverb)
        assertEquals("clean, intimate, organic", preset.production.mix)

        val expectedFocus = listOf(
            "fingerpicked acoustic guitar",
            "warm electric piano",
            "rounded bass",
            "brushed drums",
            "hand percussion",
            "clean electric guitar",
            "warm analog pads"
        )
        for (inst in expectedFocus) {
            assertTrue("Expected focus instrument '$inst' in coreInstruments", preset.coreInstruments.contains(inst))
        }
    }

    @Test
    fun `practical style presets schema contains all 10 defined styles`() {
        val expectedIds = listOf(
            "modern_country_soul",
            "emotional_pop_ballad",
            "contemporary_rnb_soul",
            "latin_pop",
            "reggaeton_pop",
            "global_dance_kpop",
            "cinematic_emotional",
            "acoustic_folk_indie",
            "blues_rock_southern_soul",
            "indian_fusion_soul"
        )

        assertEquals(10, StylePresets.ALL.size)
        for (id in expectedIds) {
            assertTrue("Expected style ID '$id' in StylePresets", StylePresets.ALL.any { it.id == id })
        }
    }
}
