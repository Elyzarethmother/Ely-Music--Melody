package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class OrchestralSynthesizer {

    companion object {
        private const val TAG = "OrchestralSynth"
        const val SAMPLE_RATE = 22050
    }

    private val synthScope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null
    private var previewJob: Job? = null

    private var audioTrack: AudioTrack? = null
    private val isPlaying = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private var isLooping: Boolean = false

    // Current piece audio buffer
    private var audioBuffer: ShortArray? = null
    private var bufferDurationMs: Long = 0L
    private var currentPositionMs: Long = 0L
    private var currentBpm: Int = 100

    // Callbacks
    var onPlaybackProgress: ((currentMs: Long, totalMs: Long, amplitudes: FloatArray) -> Unit)? = null
    var onPlaybackCompleted: (() -> Unit)? = null

    init {
        setupAudioTrack()
    }

    private fun setupAudioTrack() {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = max(minBufSize, SAMPLE_RATE * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack", e)
        }
    }

    fun renderPiece(
        tracks: List<TrackLayer>,
        bpm: Int,
        totalBeats: Float
    ) {
        currentBpm = bpm
        val secondsPerBeat = 60.0f / bpm
        val totalSeconds = totalBeats * secondsPerBeat
        val totalSamples = (totalSeconds * SAMPLE_RATE).toInt()
        bufferDurationMs = (totalSeconds * 1000).toLong()

        if (totalSamples <= 0) return

        val mixBuffer = FloatArray(totalSamples)

        val activeSolo = tracks.any { it.isSolo }

        for (track in tracks) {
            if (track.isMuted) continue
            if (activeSolo && !track.isSolo) continue
            if (track.notes.isEmpty()) continue

            val trackVolume = track.volume
            val instrument = track.instrument

            for (note in track.notes) {
                val startSample = (note.startBeat * secondsPerBeat * SAMPLE_RATE).toInt()
                val durationSeconds = note.durationBeats * secondsPerBeat
                val noteSamples = (durationSeconds * SAMPLE_RATE).toInt()
                val endSample = min(startSample + noteSamples, totalSamples)

                val freq = note.frequencyHz
                val velocity = note.velocity * trackVolume

                renderInstrumentNote(
                    mixBuffer = mixBuffer,
                    startSample = startSample,
                    endSample = endSample,
                    freq = freq,
                    durationSeconds = durationSeconds,
                    velocity = velocity,
                    instrument = instrument
                )
            }
        }

        // Apply master soft limiter and convert to 16-bit PCM
        var maxPeak = 0.001f
        for (sample in mixBuffer) {
            val abs = kotlin.math.abs(sample)
            if (abs > maxPeak) maxPeak = abs
        }

        val gain = if (maxPeak > 0.95f) 0.92f / maxPeak else 0.88f
        val pcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val clamped = (mixBuffer[i] * gain).coerceIn(-1.0f, 1.0f)
            pcm[i] = (clamped * 32767f).toInt().toShort()
        }

        audioBuffer = pcm
    }

    fun getAudioBuffer(): ShortArray? = audioBuffer
    fun getBufferDurationMs(): Long = bufferDurationMs
    fun getCurrentPositionMs(): Long = currentPositionMs

    private fun renderInstrumentNote(
        mixBuffer: FloatArray,
        startSample: Int,
        endSample: Int,
        freq: Float,
        durationSeconds: Float,
        velocity: Float,
        instrument: InstrumentType
    ) {
        val noteLength = endSample - startSample
        if (noteLength <= 0) return

        val twoPi = 2.0 * PI
        val angularFreq = twoPi * freq / SAMPLE_RATE

        for (i in 0 until noteLength) {
            val sampleIdx = startSample + i
            if (sampleIdx >= mixBuffer.size) break

            val timeSec = i.toFloat() / SAMPLE_RATE
            val progress = timeSec / durationSeconds

            // Envelope calculation (Attack, Decay, Sustain, Release)
            val envelope: Float = when (instrument) {
                InstrumentType.TIMPANI, InstrumentType.BRUSHED_DRUMS, InstrumentType.TABLA_PERCUSSION, InstrumentType.LATIN_PERCUSSION -> {
                    // Percussion strike: rapid decay
                    exp(-timeSec * 7.5f)
                }
                InstrumentType.HARP, InstrumentType.ACOUSTIC_GUITAR, InstrumentType.SITAR -> {
                    // Pluck: instantaneous attack, exponential ring
                    val attack = if (timeSec < 0.012f) timeSec / 0.012f else 1.0f
                    attack * exp(-timeSec * 3.8f)
                }
                InstrumentType.ELECTRIC_PIANO, InstrumentType.GRAND_PIANO -> {
                    // Piano / Rhodes key strike
                    val attack = if (timeSec < 0.01f) timeSec / 0.01f else 1.0f
                    attack * exp(-timeSec * 2.2f)
                }
                InstrumentType.ROUNDED_BASS, InstrumentType.SUB_BASS -> {
                    // Bass pulse
                    val attack = if (timeSec < 0.02f) timeSec / 0.02f else 1.0f
                    attack * exp(-timeSec * 1.8f)
                }
                InstrumentType.FLUTE, InstrumentType.OBOE, InstrumentType.BANSURI -> {
                    // Woodwind: gentle swell and release
                    when {
                        timeSec < 0.08f -> timeSec / 0.08f
                        progress > 0.85f -> max(0.0f, (1.0f - progress) / 0.15f)
                        else -> 1.0f
                    }
                }
                InstrumentType.STRING_SECTION, InstrumentType.CELLO, InstrumentType.ANALOG_PAD, InstrumentType.HARMONIUM -> {
                    // Orchestral Strings & Warm Pads: lush swell
                    when {
                        timeSec < 0.22f -> (timeSec / 0.22f).let { it * it }
                        progress > 0.8f -> max(0.0f, (1.0f - progress) / 0.2f)
                        else -> 1.0f
                    }
                }
                InstrumentType.CHOIR, InstrumentType.VOCAL_DUET -> {
                    // Vocal choir / duet: expressive breath swell
                    when {
                        timeSec < 0.25f -> (timeSec / 0.25f)
                        progress > 0.75f -> max(0.0f, (1.0f - progress) / 0.25f)
                        else -> 1.0f
                    }
                }
                else -> {
                    // Standard sustain (Electric Guitar, Horn, Trumpet, etc.)
                    when {
                        timeSec < 0.05f -> timeSec / 0.05f
                        progress > 0.82f -> max(0.0f, (1.0f - progress) / 0.18f)
                        else -> 1.0f
                    }
                }
            }

            // Waveform synthesis
            val sampleVal: Float = when (instrument) {
                InstrumentType.VIOLIN_SOLO -> {
                    // Lyrical saw + 5.5Hz vibrato
                    val vibrato = 1.0 + 0.018 * sin(twoPi * 5.5 * timeSec)
                    val phase = (angularFreq * vibrato * i) % twoPi
                    val saw = (phase / PI - 1.0).toFloat()
                    val sine = sin(phase).toFloat()
                    0.65f * saw + 0.35f * sine
                }
                InstrumentType.STRING_SECTION -> {
                    // Dual detuned chorus saws for ensemble texture
                    val phase1 = (angularFreq * 0.997 * i) % twoPi
                    val phase2 = (angularFreq * 1.003 * i) % twoPi
                    val saw1 = (phase1 / PI - 1.0).toFloat()
                    val saw2 = (phase2 / PI - 1.0).toFloat()
                    0.5f * (saw1 + saw2)
                }
                InstrumentType.CELLO -> {
                    // Warm rich lower saw + octave sub-harmonic
                    val phase = (angularFreq * i) % twoPi
                    val subPhase = (angularFreq * 0.5 * i) % twoPi
                    val saw = (phase / PI - 1.0).toFloat()
                    val sub = sin(subPhase).toFloat()
                    0.6f * saw + 0.4f * sub
                }
                InstrumentType.ACOUSTIC_GUITAR -> {
                    // Fingerpicked acoustic guitar: plucked triangle + woody body
                    val p1 = angularFreq * i
                    val pick = if (timeSec < 0.01f) ((Math.random() - 0.5) * 0.25).toFloat() else 0.0f
                    val body = sin(p1).toFloat() * 0.7f + sin(p1 * 2).toFloat() * 0.25f + sin(p1 * 3).toFloat() * 0.05f
                    body + pick
                }
                InstrumentType.ELECTRIC_GUITAR -> {
                    // Clean electric guitar: warm pickup tone with chime
                    val p1 = angularFreq * i
                    val trem = 1.0 + 0.05 * sin(twoPi * 4.0 * timeSec)
                    (sin(p1 * trem).toFloat() * 0.65f + sin(p1 * 2).toFloat() * 0.25f + sin(p1 * 3).toFloat() * 0.10f)
                }
                InstrumentType.SITAR -> {
                    // Sitar: sharp pluck + rich jawari buzzing harmonics
                    val p1 = angularFreq * i
                    val buzz = sin(p1).toFloat() * 0.4f +
                            sin(p1 * 2).toFloat() * 0.25f +
                            sin(p1 * 3).toFloat() * 0.20f +
                            sin(p1 * 4).toFloat() * 0.15f
                    buzz
                }
                InstrumentType.ELECTRIC_PIANO -> {
                    // Rhodes electric piano: warm tine bell harmonics + gentle stereo tremolo
                    val p1 = angularFreq * i
                    val tremolo = 1.0 + 0.08 * sin(twoPi * 4.5 * timeSec)
                    val fundamental = sin(p1).toFloat() * 0.75f
                    val tineChime = sin(p1 * 3).toFloat() * 0.18f * exp(-timeSec * 5f)
                    val bellPing = sin(p1 * 7).toFloat() * 0.07f * exp(-timeSec * 12f)
                    (fundamental + tineChime + bellPing) * tremolo.toFloat()
                }
                InstrumentType.GRAND_PIANO -> {
                    // Grand Piano: hammer strike + dual string harmonic resonance
                    val p1 = angularFreq * i
                    val hammer = if (timeSec < 0.015f) ((Math.random() - 0.5) * 0.2).toFloat() else 0.0f
                    val strings = sin(p1).toFloat() * 0.6f + sin(p1 * 2).toFloat() * 0.28f + sin(p1 * 3).toFloat() * 0.12f
                    strings + hammer
                }
                InstrumentType.HARMONIUM -> {
                    // Harmonium reed pump organ: dual reed square/sine mix with gentle breath
                    val p1 = angularFreq * i
                    val saw = ((p1 % twoPi) / PI - 1.0).toFloat()
                    val sine = sin(p1).toFloat()
                    0.5f * saw + 0.5f * sine
                }
                InstrumentType.ANALOG_PAD -> {
                    // Warm vintage analog synth pad: dual detuned chorus waves
                    val phase1 = (angularFreq * 0.996 * i) % twoPi
                    val phase2 = (angularFreq * 1.004 * i) % twoPi
                    val w1 = sin(phase1).toFloat()
                    val w2 = sin(phase2).toFloat()
                    0.5f * (w1 + w2)
                }
                InstrumentType.ROUNDED_BASS -> {
                    // Rounded bass guitar: smooth low-pass filtered warm foundation
                    val p1 = angularFreq * i
                    sin(p1).toFloat() * 0.85f + sin(p1 * 2).toFloat() * 0.15f
                }
                InstrumentType.SUB_BASS -> {
                    // Deep sub-bass: pure filtered sub-sine
                    val p1 = angularFreq * i
                    sin(p1).toFloat()
                }
                InstrumentType.FRENCH_HORN -> {
                    // Warm rounded brass: fundamental + 2nd + 3rd harmonics
                    val p1 = angularFreq * i
                    sin(p1).toFloat() * 0.65f +
                            sin(p1 * 2).toFloat() * 0.25f +
                            sin(p1 * 3).toFloat() * 0.10f
                }
                InstrumentType.TRUMPET -> {
                    // Bright fanfare: higher odd and even harmonics
                    val p1 = angularFreq * i
                    sin(p1).toFloat() * 0.5f +
                            sin(p1 * 2).toFloat() * 0.3f +
                            sin(p1 * 3).toFloat() * 0.15f +
                            sin(p1 * 4).toFloat() * 0.05f
                }
                InstrumentType.FLUTE, InstrumentType.BANSURI -> {
                    // Pure airy sine wave / bamboo flute breath
                    val p1 = angularFreq * i
                    val vib = if (instrument == InstrumentType.BANSURI) 1.0 + 0.015 * sin(twoPi * 4.8 * timeSec) else 1.0
                    val sine = sin(p1 * vib).toFloat()
                    val breath = ((Math.random() - 0.5) * 0.07).toFloat()
                    sine * 0.93f + breath
                }
                InstrumentType.OBOE -> {
                    // Plaintive reed harmonics
                    val p1 = angularFreq * i
                    sin(p1).toFloat() * 0.45f +
                            sin(p1 * 2).toFloat() * 0.35f +
                            sin(p1 * 3).toFloat() * 0.20f
                }
                InstrumentType.HARP -> {
                    // Plucked string harmonics with rapid dampening
                    val p1 = angularFreq * i
                    sin(p1).toFloat() * 0.7f +
                            sin(p1 * 2).toFloat() * 0.2f * exp(-timeSec * 8f) +
                            sin(p1 * 3).toFloat() * 0.1f * exp(-timeSec * 14f)
                }
                InstrumentType.CHOIR -> {
                    // Symphonic vocal choir formants
                    val p1 = angularFreq * i
                    val choirLfo = 1.0 + 0.012 * sin(twoPi * 4.2 * timeSec)
                    val pDetuned = (angularFreq * choirLfo * i)
                    (sin(p1).toFloat() * 0.5f +
                            sin(pDetuned).toFloat() * 0.35f +
                            sin(p1 * 3).toFloat() * 0.15f)
                }
                InstrumentType.VOCAL_DUET -> {
                    // Vocal duet: alternating male/female vocal formants with call & response
                    val p1 = angularFreq * i
                    val form1 = sin(p1).toFloat() * 0.6f + sin(p1 * 2).toFloat() * 0.25f + sin(p1 * 3).toFloat() * 0.15f
                    val lfo = 1.0 + 0.01 * sin(twoPi * 5.0 * timeSec)
                    form1 * lfo.toFloat()
                }
                InstrumentType.BRUSHED_DRUMS -> {
                    // Brushed drums: punchy kick + brushed snare noise burst
                    val pKick = twoPi * (65.0 * (1.0 + exp(-timeSec * 30.0))) * timeSec
                    val kick = sin(pKick).toFloat() * exp(-timeSec * 8f)
                    val brushSnare = if (timeSec < 0.08f) ((Math.random() - 0.5) * 0.35).toFloat() * exp(-timeSec * 15f) else 0.0f
                    kick * 0.7f + brushSnare
                }
                InstrumentType.TABLA_PERCUSSION -> {
                    // Tabla: Bayan pitch drop + resonant Dayan ring
                    val pDrop = twoPi * (85.0 * (1.0 + 0.8 * exp(-timeSec * 12.0))) * timeSec
                    val bayan = sin(pDrop).toFloat() * exp(-timeSec * 6f)
                    val dayan = sin(twoPi * 380.0 * timeSec).toFloat() * exp(-timeSec * 10f) * 0.4f
                    bayan * 0.65f + dayan
                }
                InstrumentType.LATIN_PERCUSSION -> {
                    // Conga / Dembow slap + shaker
                    val slap = sin(twoPi * 180.0 * timeSec).toFloat() * exp(-timeSec * 18f)
                    val shaker = if (timeSec < 0.04f) ((Math.random() - 0.5) * 0.3).toFloat() else 0.0f
                    slap * 0.7f + shaker
                }
                InstrumentType.TIMPANI -> {
                    // Kettle drum: pitch dive envelope from 1.6x down to base frequency
                    val pitchDrop = 1.0 + 0.7 * exp(-timeSec * 16.0)
                    val p = twoPi * (freq * pitchDrop) * timeSec
                    val body = sin(p).toFloat()
                    val click = if (timeSec < 0.02f) ((Math.random() - 0.5) * 0.4).toFloat() else 0.0f
                    body * 0.85f + click
                }
            }

            mixBuffer[sampleIdx] += sampleVal * envelope * velocity
        }
    }

    fun play() {
        val buffer = audioBuffer ?: return
        if (buffer.isEmpty()) return

        isPlaying.set(true)
        isPaused.set(false)

        try {
            audioTrack?.play()
        } catch (e: Exception) {
            setupAudioTrack()
            audioTrack?.play()
        }

        playbackJob?.cancel()
        playbackJob = synthScope.launch {
            val chunkSize = 1024
            val startSample = ((currentPositionMs.toDouble() / bufferDurationMs.coerceAtLeast(1L)) * buffer.size).toInt().coerceIn(0, buffer.size)
            var currentSampleIdx = startSample

            val amplitudeHistory = FloatArray(24)

            while (isActive && isPlaying.get()) {
                if (isPaused.get()) {
                    delay(50)
                    continue
                }

                if (currentSampleIdx >= buffer.size) {
                    if (isLooping) {
                        currentSampleIdx = 0
                        currentPositionMs = 0
                    } else {
                        isPlaying.set(false)
                        onPlaybackCompleted?.invoke()
                        break
                    }
                }

                val endIdx = min(currentSampleIdx + chunkSize, buffer.size)
                val count = endIdx - currentSampleIdx
                audioTrack?.write(buffer, currentSampleIdx, count)

                // Measure real-time RMS amplitude for visualizer
                var sumSq = 0f
                for (j in currentSampleIdx until endIdx step 16) {
                    val s = buffer[j] / 32768f
                    sumSq += s * s
                }
                val rms = kotlin.math.sqrt(sumSq / (count / 16).coerceAtLeast(1))

                // Shift visualizer bands
                for (k in 0 until amplitudeHistory.size - 1) {
                    amplitudeHistory[k] = amplitudeHistory[k + 1]
                }
                amplitudeHistory[amplitudeHistory.size - 1] = (rms * 2.8f).coerceIn(0.05f, 1.0f)

                currentSampleIdx = endIdx
                currentPositionMs = ((currentSampleIdx.toDouble() / buffer.size) * bufferDurationMs).toLong()

                onPlaybackProgress?.invoke(currentPositionMs, bufferDurationMs, amplitudeHistory.copyOf())
            }
        }
    }

    fun pause() {
        isPaused.set(true)
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun resume() {
        if (isPaused.get()) {
            isPaused.set(false)
            try {
                audioTrack?.play()
            } catch (_: Exception) {}
        } else {
            play()
        }
    }

    fun stop() {
        isPlaying.set(false)
        isPaused.set(false)
        playbackJob?.cancel()
        currentPositionMs = 0L
        try {
            audioTrack?.stop()
            audioTrack?.flush()
        } catch (_: Exception) {}
        onPlaybackProgress?.invoke(0L, bufferDurationMs, FloatArray(24))
    }

    fun seekTo(positionMs: Long) {
        currentPositionMs = positionMs.coerceIn(0L, bufferDurationMs)
        if (isPlaying.get() && !isPaused.get()) {
            // Restart playback at new seek point
            play()
        } else {
            onPlaybackProgress?.invoke(currentPositionMs, bufferDurationMs, FloatArray(24))
        }
    }

    fun setLooping(loop: Boolean) {
        isLooping = loop
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying.get() && !isPaused.get()

    /**
     * Instantly play a short note preview for interactive instrument & pitch feedback
     */
    fun playPreviewNote(pitchMidi: Int, instrument: InstrumentType, durationSec: Float = 0.8f) {
        previewJob?.cancel()
        previewJob = synthScope.launch {
            val totalSamples = (durationSec * SAMPLE_RATE).toInt()
            val mix = FloatArray(totalSamples)
            val freq = (440.0 * Math.pow(2.0, (pitchMidi - 69).toDouble() / 12.0)).toFloat()

            renderInstrumentNote(
                mixBuffer = mix,
                startSample = 0,
                endSample = totalSamples,
                freq = freq,
                durationSeconds = durationSec,
                velocity = 0.85f,
                instrument = instrument
            )

            val pcm = ShortArray(totalSamples)
            for (i in 0 until totalSamples) {
                pcm[i] = (mix[i].coerceIn(-1.0f, 1.0f) * 28000f).toInt().toShort()
            }

            try {
                val previewTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                previewTrack.write(pcm, 0, totalSamples)
                previewTrack.play()
                delay((durationSec * 1000).toLong() + 100)
                previewTrack.stop()
                previewTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing preview note", e)
            }
        }
    }

    fun release() {
        stop()
        audioTrack?.release()
        audioTrack = null
    }
}
