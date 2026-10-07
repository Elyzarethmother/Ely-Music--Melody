package com.example.audio

import kotlin.math.pow

data class NoteEvent(
    val pitchMidi: Int, // 60 = C4, 69 = A4 (440Hz)
    val startBeat: Float, // beat position relative to piece start
    val durationBeats: Float,
    val velocity: Float = 0.8f
) {
    val frequencyHz: Float
        get() = (440.0 * 2.0.pow((pitchMidi - 69).toDouble() / 12.0)).toFloat()

    val noteName: String
        get() {
            val names = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val octave = (pitchMidi / 12) - 1
            val note = names[pitchMidi % 12]
            return "$note$octave"
        }
}

object MusicalScales {
    // Root MIDI numbers for Octave 4
    const val NOTE_C4 = 60
    const val NOTE_D4 = 62
    const val NOTE_E4 = 64
    const val NOTE_F4 = 65
    const val NOTE_G4 = 67
    const val NOTE_A4 = 69
    const val NOTE_B4 = 71

    val MAJOR_INTERVALS = listOf(0, 2, 4, 5, 7, 9, 11)
    val NATURAL_MINOR_INTERVALS = listOf(0, 2, 3, 5, 7, 8, 10)
    val HARMONIC_MINOR_INTERVALS = listOf(0, 2, 3, 5, 7, 8, 11)
    val DORIAN_INTERVALS = listOf(0, 2, 3, 5, 7, 9, 10)
    val PENTATONIC_MAJOR_INTERVALS = listOf(0, 2, 4, 7, 9)

    fun getScalePitches(rootMidi: Int, intervals: List<Int>, octaves: Int = 2): List<Int> {
        val result = mutableListOf<Int>()
        for (oct in 0 until octaves) {
            for (interval in intervals) {
                result.add(rootMidi + (oct * 12) + interval)
            }
        }
        return result
    }

    fun parseRootMidi(keyName: String): Int {
        val root = keyName.split(" ").firstOrNull()?.uppercase() ?: "C"
        return when (root) {
            "C" -> 60
            "C#", "DB" -> 61
            "D" -> 62
            "D#", "EB" -> 63
            "E" -> 64
            "F" -> 65
            "F#", "GB" -> 66
            "G" -> 67
            "G#", "AB" -> 68
            "A" -> 69
            "A#", "BB" -> 70
            "B" -> 71
            else -> 60
        }
    }
}
