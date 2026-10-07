package com.example

import com.example.ai.GeminiOrchestrator
import com.example.audio.InstrumentType
import com.example.audio.OrchestralSynthesizer
import com.example.model.StylePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ModernCountrySoulVerificationTest {

    @Test
    fun `verify Modern Country Soul 82 BPM case, dual variations, local PCM audio synthesis, and transport lifecycle`() {
        println("=== [START VERIFICATION] Modern Country Soul 82 BPM Case ===")

        // 1. Exercise the Modern Country Soul 82 BPM Reference Specification
        val preset = StylePresets.ALL.find { it.id == "modern_country_soul" }
        assertNotNull("Preset modern_country_soul must exist in StylePresets", preset)
        requireNotNull(preset)

        assertEquals("Modern Country Soul", preset.name)
        assertEquals(82, preset.defaultBpm)
        assertEquals(Pair(78, 92), preset.bpmRange)
        assertEquals("slow-dance sway", preset.groove)
        assertEquals("intimate, warm, nostalgic, tender", preset.mood)
        assertTrue("Preset must be marked as duet", preset.isDuet)
        assertEquals("natural male-female duet with gentle call and response", preset.vocalCharacter)
        assertEquals("subtle", preset.production.tapeDelay)
        assertEquals("soft plate", preset.production.reverb)
        assertEquals("clean, intimate, organic", preset.production.mix)

        println("[OK] Preset Specification verified: 82 BPM, slow-dance sway, Duet=true, clean intimate mix.")

        // 2. Generate Variation A and Variation B (Local Algorithmic Engine, No External API)
        val orchestrator = GeminiOrchestrator()

        val pieceA = orchestrator.generateAlgorithmicPiece(
            seed = 8201L,
            prompt = preset.defaultPrompt,
            title = "Tennessee Amber (Var A)",
            lyrics = "[Verse 1 - Male]\nPorch light glowing in the twilight air\n[Verse 1 - Female]\nGentle breeze rustling through your hair\n[Chorus - Duet]\nSlow-dance sway till the morning light",
            tempoBpm = 82,
            keySignature = "G Major",
            emotionalMood = preset.mood,
            timeSignature = "4/4",
            isInstrumental = false,
            selectedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.ANALOG_PAD
            ),
            variationLabel = "Variation A"
        )

        val pieceB = orchestrator.generateAlgorithmicPiece(
            seed = 8202L,
            prompt = preset.defaultPrompt,
            title = "Autumn Embers (Var B)",
            lyrics = "[Verse 1 - Female]\nLeaves falling soft across the gravel lane\n[Verse 1 - Male]\nGuitar chords humming through the window pane\n[Chorus - Duet]\nHold me close in the slow-dance sway",
            tempoBpm = 82,
            keySignature = "G Major",
            emotionalMood = preset.mood,
            timeSignature = "4/4",
            isInstrumental = false,
            selectedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.ANALOG_PAD
            ),
            variationLabel = "Variation B"
        )

        assertNotNull("Variation A must be generated", pieceA)
        assertNotNull("Variation B must be generated", pieceB)
        assertEquals(82, pieceA.tempoBpm)
        assertEquals(82, pieceB.tempoBpm)
        assertEquals(5, pieceA.tracks.size)
        assertEquals(5, pieceB.tracks.size)
        assertTrue("Variation A must have melody notes", pieceA.tracks[0].notes.isNotEmpty())
        assertTrue("Variation B must have melody notes", pieceB.tracks[0].notes.isNotEmpty())

        println("[OK] Variation A generated: Title='${pieceA.title}', Tempo=${pieceA.tempoBpm} BPM, Stems=${pieceA.tracks.size}")
        println("[OK] Variation B generated: Title='${pieceB.title}', Tempo=${pieceB.tempoBpm} BPM, Stems=${pieceB.tracks.size}")

        // 3. Start Actual Local PCM Audio Synthesis for Variation A
        val synthesizer = OrchestralSynthesizer()
        synthesizer.renderPiece(
            tracks = pieceA.tracks,
            bpm = pieceA.tempoBpm,
            totalBeats = pieceA.totalBeats
        )

        val audioBuffer = synthesizer.getAudioBuffer()
        assertNotNull("Rendered audio PCM buffer must not be null", audioBuffer)
        requireNotNull(audioBuffer)
        assertTrue("Rendered audio PCM buffer must not be empty", audioBuffer.isNotEmpty())

        val sampleCount = audioBuffer.size
        val durationMs = synthesizer.getBufferDurationMs()
        val sampleRate = OrchestralSynthesizer.SAMPLE_RATE

        // Measure Audio Waveform Dynamics
        var peakSample = 0
        var nonZeroCount = 0
        var sumSquares = 0.0

        for (sample in audioBuffer) {
            val sInt = sample.toInt()
            val absVal = abs(sInt)
            if (absVal > peakSample) peakSample = absVal
            if (absVal > 50) nonZeroCount++
            val norm = sInt / 32768.0
            sumSquares += norm * norm
        }

        val rms = sqrt(sumSquares / sampleCount)
        val nonZeroRatio = nonZeroCount.toDouble() / sampleCount

        println("[OK] PCM Audio Synthesis Verified:")
        println("     - Sample Rate: $sampleRate Hz (16-bit PCM)")
        println("     - Total Samples: $sampleCount")
        println("     - Duration: $durationMs ms (${durationMs / 1000.0} seconds)")
        println("     - Peak Sample Amplitude: $peakSample / 32767 (${(peakSample / 32767.0 * 100).toInt()}%)")
        println("     - Active Audio Ratio: ${(nonZeroRatio * 100).toInt()}% non-silent")
        println("     - RMS Dynamic Energy: ${"%.4f".format(rms)}")

        assertTrue("Sample count must be greater than 100,000 for full 8-bar audio", sampleCount > 100000)
        assertTrue("Duration must match expected 82 BPM 8-bar duration (~23.4s)", durationMs in 22000L..25000L)
        assertTrue("Audio must not be silent; peak sample must exceed 10000", peakSample > 10000)
        assertTrue("Active audio ratio must exceed 75%", nonZeroRatio > 0.75)
        assertTrue("RMS energy must be positive and dynamic", rms > 0.05)

        // 4. Verify Transport Lifecycle: Play, Pause, Seek, Resume, Stop
        // Play
        synthesizer.play()
        assertTrue("isCurrentlyPlaying must be true after play()", synthesizer.isCurrentlyPlaying())
        println("[OK] Play state verified: isCurrentlyPlaying = ${synthesizer.isCurrentlyPlaying()}")

        // Pause
        synthesizer.pause()
        assertFalse("isCurrentlyPlaying must be false after pause()", synthesizer.isCurrentlyPlaying())
        println("[OK] Pause state verified: isCurrentlyPlaying = ${synthesizer.isCurrentlyPlaying()}")

        // Seek
        val seekTargetMs = 10000L
        synthesizer.seekTo(seekTargetMs)
        assertEquals("Seek position must update to target", seekTargetMs, synthesizer.getCurrentPositionMs())
        println("[OK] Seek state verified: currentPositionMs = ${synthesizer.getCurrentPositionMs()}")

        // Resume from seek
        synthesizer.resume()
        assertTrue("isCurrentlyPlaying must be true after resume()", synthesizer.isCurrentlyPlaying())
        println("[OK] Resume state verified: isCurrentlyPlaying = ${synthesizer.isCurrentlyPlaying()}")

        // Stop
        synthesizer.stop()
        assertFalse("isCurrentlyPlaying must be false after stop()", synthesizer.isCurrentlyPlaying())
        assertEquals("Stop must reset playhead to 0", 0L, synthesizer.getCurrentPositionMs())
        println("[OK] Stop state verified: isCurrentlyPlaying = ${synthesizer.isCurrentlyPlaying()}, position = ${synthesizer.getCurrentPositionMs()}")

        synthesizer.release()
        println("=== [VERIFICATION COMPLETE] All criteria met with positive evidence. ===")
    }
}
