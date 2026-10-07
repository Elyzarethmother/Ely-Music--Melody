package com.example

import com.example.ai.GeminiOrchestrator
import com.example.audio.AudioExporter
import com.example.audio.InstrumentType
import com.example.audio.OrchestralSynthesizer
import com.example.audio.TrackType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testSongGenerationFromLyricsAndStyleWithAudioExport() {
    val orchestrator = GeminiOrchestrator()
    val lyrics = """
      [Verse 1]
      Dust on the highway, twilight falling slow
      Chasing gentle memories from the songs we used to know
      [Chorus]
      Hold me in the harmony, singing through the night
      Two souls dancing in the warm amber light
    """.trimIndent()

    val piece = orchestrator.generateAlgorithmicPiece(
      seed = 42L,
      prompt = "Modern Country Soul with acoustic guitar, Rhodes piano, and soulful duet",
      title = "Dust & Amber Glow",
      lyrics = lyrics,
      tempoBpm = 82,
      keySignature = "G Major",
      emotionalMood = "Intimate & Warm",
      selectedInstruments = listOf(
        InstrumentType.VOCAL_DUET,
        InstrumentType.ACOUSTIC_GUITAR,
        InstrumentType.ELECTRIC_PIANO,
        InstrumentType.ROUNDED_BASS,
        InstrumentType.BRUSHED_DRUMS
      )
    )

    // 1. Verify metadata and lyrics
    assertEquals("Dust & Amber Glow", piece.title)
    assertEquals(82, piece.tempoBpm)
    assertEquals("G Major", piece.keySignature)
    assertTrue("Piece must contain lyrics", piece.lyrics.contains("Dust on the highway"))

    // 2. Verify track layers include vocal melody and music accompaniment
    val vocalTrack = piece.tracks.firstOrNull { it.type == TrackType.VOCAL }
    assertNotNull("Piece must contain dedicated vocal track for lyrics", vocalTrack)
    assertEquals(InstrumentType.VOCAL_DUET, vocalTrack?.instrument)
    assertTrue("Vocal track must contain melody notes", vocalTrack?.notes?.isNotEmpty() == true)

    val melodyTrack = piece.tracks.firstOrNull { it.type == TrackType.MELODY }
    assertNotNull("Piece must contain lead instrument accompaniment track", melodyTrack)

    val bassTrack = piece.tracks.firstOrNull { it.type == TrackType.BASS }
    assertNotNull("Piece must contain bass track", bassTrack)

    // 3. Render audio using OrchestralSynthesizer
    val synth = OrchestralSynthesizer()
    synth.renderPiece(
      tracks = piece.tracks,
      bpm = piece.tempoBpm,
      totalBeats = piece.totalBeats
    )

    val pcm = synth.getAudioBuffer()
    assertNotNull("Synthesizer must produce PCM audio buffer", pcm)
    assertTrue("PCM audio buffer must contain audio samples", (pcm?.size ?: 0) > 0)

    var hasNonZeroSamples = false
    var peakSample = 0
    pcm?.forEach { sample ->
      if (sample.toInt() != 0) hasNonZeroSamples = true
      val abs = kotlin.math.abs(sample.toInt())
      if (abs > peakSample) peakSample = abs
    }
    assertTrue("Audio buffer must have audible sound samples", hasNonZeroSamples)
    assertTrue("Audio peak must be audible (> 1000 in 16-bit range)", peakSample > 1000)

    // 4. Verify AudioExporter produces complete audio file byte stream
    val audioBytes = AudioExporter.createWavBytes(pcm!!)
    assertTrue("Exported audio must be larger than 44 bytes header", audioBytes.size > 44)
    assertEquals('R'.code.toByte(), audioBytes[0])
    assertEquals('I'.code.toByte(), audioBytes[1])
    assertEquals('F'.code.toByte(), audioBytes[2])
    assertEquals('F'.code.toByte(), audioBytes[3])
    assertEquals('W'.code.toByte(), audioBytes[8])
    assertEquals('A'.code.toByte(), audioBytes[9])
    assertEquals('V'.code.toByte(), audioBytes[10])
    assertEquals('E'.code.toByte(), audioBytes[11])
  }
}

