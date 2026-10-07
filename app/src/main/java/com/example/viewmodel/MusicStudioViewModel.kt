package com.example.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiOrchestrator
import com.example.audio.AudioExporter
import com.example.audio.InstrumentType
import com.example.audio.OrchestralSynthesizer
import com.example.audio.TrackLayer
import com.example.audio.TrackType
import com.example.model.HarmonicStyle
import com.example.model.LyricLineAnalysis
import com.example.model.LyricUtils
import com.example.model.OrchestralPiece
import com.example.model.PieceSection
import com.example.model.StylePreset
import com.example.model.StylePresets
import com.example.repository.PieceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab(val label: String, val icon: String) {
    CREATE("Create", "sparkles"),
    PLAYER("Studio", "play"),
    HARMONY("Harmony", "layers"),
    LYRICS("Lyrics", "mic"),
    STYLE("Style Lab", "tune"),
    LIBRARY("Library", "music_library")
}

data class GenerationState(
    val isGenerating: Boolean = false,
    val statusText: String = "",
    val generatedPair: Pair<OrchestralPiece, OrchestralPiece>? = null,
    val errorMessage: String? = null
)

class MusicStudioViewModel(
    private val orchestrator: GeminiOrchestrator = GeminiOrchestrator(),
    private val repository: PieceRepository = PieceRepository(orchestrator)
) : ViewModel() {

    val synthesizer = OrchestralSynthesizer()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(StudioTab.CREATE)
    val currentTab: StateFlow<StudioTab> = _currentTab.asStateFlow()

    // Pieces Library
    val pieces: StateFlow<List<OrchestralPiece>> = repository.pieces

    // Active Playing Piece
    private val _currentPiece = MutableStateFlow<OrchestralPiece?>(null)
    val currentPiece: StateFlow<OrchestralPiece?> = _currentPiece.asStateFlow()

    // Playback State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _totalTimeMs = MutableStateFlow(0L)
    val totalTimeMs: StateFlow<Long> = _totalTimeMs.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _visualizerAmplitudes = MutableStateFlow(FloatArray(24))
    val visualizerAmplitudes: StateFlow<FloatArray> = _visualizerAmplitudes.asStateFlow()

    // Creation Input State (Lyrics + Music -> MP3)
    val customTitleInput = MutableStateFlow("Midnight River Harmony")
    val customLyricsInput = MutableStateFlow(
        """
        [Verse 1]
        Dust on the highway, twilight falling slow
        Chasing gentle memories from the songs we used to know
        Foot on the pedal, sway into the groove
        Every tender whisper has a heartache to remove

        [Chorus]
        Hold me in the harmony, singing through the night
        Two souls dancing in the warm amber light
        Country soul and tender grace, let the river flow
        Every note of love is everywhere we go
        """.trimIndent()
    )
    val promptInput = MutableStateFlow("Modern Country Soul: warm fingerpicked acoustic guitar, Rhodes electric piano, rounded bass, brushed drums, and soulful vocal duet.")
    val isCustomMode = MutableStateFlow(true)
    val isInstrumental = MutableStateFlow(false)
    val vocalStyle = MutableStateFlow("Duet (Call & Response)")

    // Style Parameters
    val selectedPreset = MutableStateFlow(StylePresets.ALL[0])
    val tempoBpm = MutableStateFlow(82)
    val keySignature = MutableStateFlow("G Major")
    val emotionalMood = MutableStateFlow("Intimate & Warm")
    val timeSignature = MutableStateFlow("4/4")
    val selectedInstruments = MutableStateFlow<Set<InstrumentType>>(
        setOf(
            InstrumentType.VOCAL_DUET,
            InstrumentType.ACOUSTIC_GUITAR,
            InstrumentType.ELECTRIC_PIANO,
            InstrumentType.ROUNDED_BASS,
            InstrumentType.BRUSHED_DRUMS
        )
    )

    // Export & Sharing
    val lastExportedFile = MutableStateFlow<File?>(null)
    val exportStatusMessage = MutableStateFlow<String?>(null)

    // Generation State
    private val _generationState = MutableStateFlow(GenerationState())
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Lyrics Analysis State
    private val _lyricAnalysis = MutableStateFlow<List<LyricLineAnalysis>>(emptyList())
    val lyricAnalysis: StateFlow<List<LyricLineAnalysis>> = _lyricAnalysis.asStateFlow()

    // Sophisticated Classical / Romantic Harmony Generator State
    val selectedHarmonicStyle = MutableStateFlow(HarmonicStyle.ROMANTIC_BRAHMS)
    val useContraryMotion = MutableStateFlow(true)
    val useSuspensions = MutableStateFlow(true)
    val useHarpArpeggios = MutableStateFlow(true)
    val isHarmonizing = MutableStateFlow(false)

    init {
        setupSynthesizerCallbacks()

        // Auto-select initial piece from library
        viewModelScope.launch {
            repository.pieces.collectLatest { list ->
                if (_currentPiece.value == null && list.isNotEmpty()) {
                    loadPiece(list.first(), autoPlay = false)
                }
            }
        }
    }

    private fun setupSynthesizerCallbacks() {
        synthesizer.onPlaybackProgress = { current, total, amps ->
            _currentTimeMs.value = current
            _totalTimeMs.value = total
            _visualizerAmplitudes.value = amps
            _isPlaying.value = synthesizer.isCurrentlyPlaying()
        }

        synthesizer.onPlaybackCompleted = {
            _isPlaying.value = false
            _currentTimeMs.value = 0L
        }
    }

    fun setTab(tab: StudioTab) {
        _currentTab.value = tab
    }

    fun applyPreset(preset: StylePreset) {
        selectedPreset.value = preset
        promptInput.value = preset.defaultPrompt
        tempoBpm.value = preset.tempoBpm
        keySignature.value = preset.keySignature
        emotionalMood.value = preset.emotionalMood
        timeSignature.value = preset.timeSignature
        selectedInstruments.value = preset.primaryInstruments.toSet()
    }

    fun toggleInstrumentSelection(instrument: InstrumentType) {
        val current = selectedInstruments.value.toMutableSet()
        if (current.contains(instrument)) {
            if (current.size > 1) current.remove(instrument)
        } else {
            current.add(instrument)
        }
        selectedInstruments.value = current
    }

    fun generatePieces() {
        if (_generationState.value.isGenerating) return

        viewModelScope.launch {
            _generationState.value = GenerationState(
                isGenerating = true,
                statusText = "Orchestrating melody & harmony layers..."
            )

            try {
                val hasLyrics = !isInstrumental.value && customLyricsInput.value.isNotBlank()
                val vocalInst = when {
                    vocalStyle.value.contains("Duet", ignoreCase = true) -> InstrumentType.VOCAL_DUET
                    vocalStyle.value.contains("Choir", ignoreCase = true) -> InstrumentType.CHOIR
                    else -> InstrumentType.LEAD_VOCAL
                }

                val effectiveInstruments = selectedInstruments.value.toMutableList()
                if (hasLyrics && !effectiveInstruments.contains(vocalInst)) {
                    effectiveInstruments.add(0, vocalInst)
                }

                val title = customTitleInput.value.ifBlank {
                    if (hasLyrics) "Song: " + customLyricsInput.value.lineSequence().firstOrNull { it.isNotBlank() && !it.startsWith("[") }?.take(24) ?: "New Composition"
                    else "New Orchestral Piece"
                }

                val pair = orchestrator.generateOrchestralPair(
                    userPrompt = promptInput.value,
                    customTitle = title,
                    customLyrics = if (isInstrumental.value) null else customLyricsInput.value.ifBlank { null },
                    tempoBpm = tempoBpm.value,
                    keySignature = keySignature.value,
                    emotionalMood = emotionalMood.value,
                    timeSignature = timeSignature.value,
                    isInstrumental = isInstrumental.value,
                    selectedInstruments = effectiveInstruments
                )

                // Add to repository
                repository.addPiece(pair.first)
                repository.addPiece(pair.second)

                _generationState.value = GenerationState(
                    isGenerating = false,
                    statusText = "Composed successfully!",
                    generatedPair = pair
                )

                // Automatically load and play Variation A
                loadPiece(pair.first, autoPlay = true)

            } catch (e: Exception) {
                _generationState.value = GenerationState(
                    isGenerating = false,
                    errorMessage = "Error composing: ${e.message}"
                )
            }
        }
    }

    fun loadPiece(piece: OrchestralPiece, autoPlay: Boolean = true) {
        _currentPiece.value = piece
        customLyricsInput.value = piece.lyrics
        _lyricAnalysis.value = LyricUtils.analyzeLyrics(piece.lyrics)

        synthesizer.stop()
        synthesizer.setLooping(_isLooping.value)
        synthesizer.renderPiece(
            tracks = piece.tracks,
            bpm = piece.tempoBpm,
            totalBeats = piece.totalBeats
        )

        if (autoPlay) {
            synthesizer.play()
            _isPlaying.value = true
        }
    }

    fun togglePlayPause() {
        if (_currentPiece.value == null) {
            val first = pieces.value.firstOrNull() ?: return
            loadPiece(first, autoPlay = true)
            return
        }

        if (synthesizer.isCurrentlyPlaying()) {
            synthesizer.pause()
            _isPlaying.value = false
        } else {
            synthesizer.resume()
            _isPlaying.value = true
        }
    }

    fun seekTo(positionMs: Long) {
        synthesizer.seekTo(positionMs)
        _currentTimeMs.value = positionMs
    }

    fun toggleLoop() {
        val newLoop = !_isLooping.value
        _isLooping.value = newLoop
        synthesizer.setLooping(newLoop)
    }

    // --- Harmony Layering Controls ---

    fun updateTrackVolume(trackId: String, volume: Float) {
        val piece = _currentPiece.value ?: return
        val updatedTracks = piece.tracks.map {
            if (it.id == trackId) it.copy(volume = volume) else it
        }
        val updatedPiece = piece.copy(tracks = updatedTracks)
        _currentPiece.value = updatedPiece
        repository.updatePiece(updatedPiece)
        reRenderCurrentPiece()
    }

    fun toggleTrackMute(trackId: String) {
        val piece = _currentPiece.value ?: return
        val updatedTracks = piece.tracks.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
        val updatedPiece = piece.copy(tracks = updatedTracks)
        _currentPiece.value = updatedPiece
        repository.updatePiece(updatedPiece)
        reRenderCurrentPiece()
    }

    fun toggleTrackSolo(trackId: String) {
        val piece = _currentPiece.value ?: return
        val updatedTracks = piece.tracks.map {
            if (it.id == trackId) it.copy(isSolo = !it.isSolo) else it
        }
        val updatedPiece = piece.copy(tracks = updatedTracks)
        _currentPiece.value = updatedPiece
        repository.updatePiece(updatedPiece)
        reRenderCurrentPiece()
    }

    fun changeTrackInstrument(trackId: String, newInstrument: InstrumentType) {
        val piece = _currentPiece.value ?: return
        val updatedTracks = piece.tracks.map {
            if (it.id == trackId) it.copy(instrument = newInstrument, name = "${it.type.defaultTitle}: ${newInstrument.displayName}") else it
        }
        val updatedPiece = piece.copy(tracks = updatedTracks)
        _currentPiece.value = updatedPiece
        repository.updatePiece(updatedPiece)
        reRenderCurrentPiece()

        // Preview sound
        synthesizer.playPreviewNote(64, newInstrument)
    }

    fun reHarmonizePiece(style: HarmonicStyle = selectedHarmonicStyle.value) {
        val piece = _currentPiece.value ?: return
        if (isHarmonizing.value) return

        viewModelScope.launch {
            isHarmonizing.value = true
            selectedHarmonicStyle.value = style

            val (newTracks, newChords) = orchestrator.generateSophisticatedHarmonies(
                piece = piece,
                style = style,
                useContraryMotion = useContraryMotion.value,
                useSuspensions = useSuspensions.value,
                useHarpArpeggios = useHarpArpeggios.value
            )

            val updatedPiece = piece.copy(
                tracks = newTracks,
                chordProgression = newChords,
                harmonicStyleName = style.title
            )

            _currentPiece.value = updatedPiece
            repository.updatePiece(updatedPiece)
            reRenderCurrentPiece()

            isHarmonizing.value = false
        }
    }

    private fun reRenderCurrentPiece() {
        val piece = _currentPiece.value ?: return
        val wasPlaying = synthesizer.isCurrentlyPlaying()
        synthesizer.renderPiece(
            tracks = piece.tracks,
            bpm = piece.tempoBpm,
            totalBeats = piece.totalBeats
        )
        if (wasPlaying) {
            synthesizer.play()
        }
    }

    // --- Lyrics Composer Controls ---

    fun updateLyrics(newLyrics: String) {
        customLyricsInput.value = newLyrics
        _lyricAnalysis.value = LyricUtils.analyzeLyrics(newLyrics)
        val piece = _currentPiece.value
        if (piece != null) {
            val updated = piece.copy(lyrics = newLyrics)
            _currentPiece.value = updated
            repository.updatePiece(updated)
        }
    }

    fun insertSectionTag(tag: String) {
        val current = customLyricsInput.value
        val toInsert = "\n\n$tag\n"
        val updated = (current + toInsert).trimStart()
        updateLyrics(updated)
    }

    fun requestAiLyricSuggestion() {
        viewModelScope.launch {
            val piece = _currentPiece.value
            val mood = piece?.emotionalMood ?: emotionalMood.value
            val suggestion = orchestrator.suggestLyricContinuation(
                currentLyrics = customLyricsInput.value,
                mood = mood
            )
            val current = customLyricsInput.value
            val combined = if (current.isBlank()) suggestion else "$current\n\n$suggestion"
            updateLyrics(combined)
        }
    }

    // --- Lyric Template Presets ---
    fun loadLyricPreset(genre: String) {
        when (genre) {
            "Country Soul" -> {
                customTitleInput.value = "Dust & Amber Glow"
                customLyricsInput.value = """
                    [Verse 1]
                    Dust on the highway, twilight falling slow
                    Chasing gentle memories from the songs we used to know
                    Foot on the pedal, sway into the groove
                    Every tender whisper has a heartache to remove

                    [Chorus]
                    Hold me in the harmony, singing through the night
                    Two souls dancing in the warm amber light
                    Country soul and tender grace, let the river flow
                    Every note of love is everywhere we go
                """.trimIndent()
                vocalStyle.value = "Duet (Call & Response)"
                applyPreset(StylePresets.ALL[0])
            }
            "Pop Ballad" -> {
                customTitleInput.value = "Shadows in the Rain"
                customLyricsInput.value = """
                    [Verse 1]
                    Piano notes fall quiet on the window pane
                    Counting every second till you call my name
                    The city sleeps in silver, streets of blue and gray
                    Waiting for the morning sun to guide the way

                    [Chorus]
                    I will stay right beside you through the rising storm
                    Even in the freezing cold our melody is warm
                    Lift your eyes and see the dawn breaking through the haze
                    We will find forever in these quiet days
                """.trimIndent()
                vocalStyle.value = "Solo Lead Singer"
                applyPreset(StylePresets.ALL[1])
            }
            "R&B Soul" -> {
                customTitleInput.value = "Velvet Midnight Pocket"
                customLyricsInput.value = """
                    [Verse 1]
                    Rhodes electric chords floating in the air
                    Candle on the table, fingers through your hair
                    Laid-back in the pocket, rhythm so sweet
                    Every little heartbeat lands upon the beat

                    [Chorus]
                    Smooth soul magic when you're by my side
                    No more secrets left for us to hide
                    Golden honey whispers underneath the stars
                    Music heals the bruises on these tender scars
                """.trimIndent()
                vocalStyle.value = "Solo Lead Singer"
                applyPreset(StylePresets.ALL[2])
            }
            "Latin Pop" -> {
                customTitleInput.value = "Corazón y Fuego"
                customLyricsInput.value = """
                    [Verse 1]
                    Nylon guitar strings strumming in the breeze
                    Warm romantic ocean under palm trees
                    Look into my eyes and let the rhythm start
                    Feel the sweet percussion beating in my heart

                    [Chorus]
                    Baila conmigo, under golden skies
                    See the passion burning in your lovely eyes
                    From Havana to the shore, we will never part
                    One song, one passion, beating in my heart
                """.trimIndent()
                vocalStyle.value = "Duet (Call & Response)"
                applyPreset(StylePresets.ALL[3])
            }
            "Cinematic Anthem" -> {
                customTitleInput.value = "Triumph of the Stars"
                customLyricsInput.value = """
                    [Verse 1]
                    Over distant mountains, banners take the sky
                    Through the iron tempest where the eagles fly
                    Sons of dawn awaking, fearless in the gale
                    Hear the horn resounding down the ancient vale

                    [Chorus]
                    Sing to the heavens, let the trumpets blow
                    Glory forever where the heroes go
                    Choir of the ages, banner raised on high
                    We shall conquer darkness and illume the sky
                """.trimIndent()
                vocalStyle.value = "Epic Choir"
                applyPreset(StylePresets.ALL.firstOrNull { it.id == "cinematic_emotional" } ?: StylePresets.ALL[0])
            }
            "Reggaeton" -> {
                customTitleInput.value = "Fuego en la Noche"
                customLyricsInput.value = """
                    [Verse 1]
                    Bass vibrating through the floor, neon glowing bright
                    Moving to the dembow beat beneath the city light
                    Whisper secrets in my ear, let the rhythm sway
                    Dancing till the sunrise comes to wash the night away

                    [Chorus]
                    Dale fuego, rhythm in the street
                    Every heart is pounding to the dembow beat
                    Move your body closer, never let it slow
                    Feel the summer fever in the midnight glow
                """.trimIndent()
                vocalStyle.value = "Solo Lead Singer"
                applyPreset(StylePresets.ALL.firstOrNull { it.id == "reggaeton_pop" } ?: StylePresets.ALL[0])
            }
            "Dance / K-Pop" -> {
                customTitleInput.value = "Starlight Electra"
                customLyricsInput.value = """
                    [Verse 1]
                    Neon lasers cutting through the midnight air
                    Synth arpeggios spinning everywhere
                    Catch the four-on-the-floor, step into the glow
                    We don't need tomorrow when the lights are low

                    [Chorus]
                    Spark in the dark, higher than the sky
                    Watch our galaxy of colors flying high
                    Dance until the morning, let the music play
                    Electro heartbeats never fade away
                """.trimIndent()
                vocalStyle.value = "Solo Lead Singer"
                applyPreset(StylePresets.ALL.firstOrNull { it.id == "global_dance_kpop" } ?: StylePresets.ALL[0])
            }
            "Acoustic Folk" -> {
                customTitleInput.value = "Canyon Pines & Morning Sun"
                customLyricsInput.value = """
                    [Verse 1]
                    Morning coffee steaming in a wooden cup
                    Watching mountain shadows as the sun wakes up
                    Worn-out denim jacket and a trusted song
                    Knowing that this quiet road is where I belong

                    [Chorus]
                    Wildflower valley, river running clear
                    All the sweetest melodies are gathered here
                    Strum the simple chords and let your spirit fly
                    Underneath the shelter of the open sky
                """.trimIndent()
                vocalStyle.value = "Duet (Call & Response)"
                applyPreset(StylePresets.ALL.firstOrNull { it.id == "acoustic_folk_indie" } ?: StylePresets.ALL[0])
            }
            "Clear" -> {
                customTitleInput.value = ""
                customLyricsInput.value = ""
            }
        }
        _lyricAnalysis.value = LyricUtils.analyzeLyrics(customLyricsInput.value)
    }

    // --- Audio Export & MP3 Sharing ---
    fun exportCurrentSong(context: Context, asMp3: Boolean = true): File? {
        val piece = _currentPiece.value ?: return null
        val buffer = synthesizer.getAudioBuffer() ?: return null
        val ext = if (asMp3) "mp3" else "wav"
        val file = AudioExporter.exportSongFile(context, piece, buffer, extension = ext)
        lastExportedFile.value = file
        if (file != null) {
            exportStatusMessage.value = "✔ MP3 File Ready: ${file.name} (${file.length() / 1024} KB)"
        }
        return file
    }

    fun saveCurrentSongToMusicFolder(context: Context, asMp3: Boolean = true) {
        val piece = _currentPiece.value ?: return
        val buffer = synthesizer.getAudioBuffer() ?: return
        val ext = if (asMp3) "mp3" else "wav"
        val uri = AudioExporter.saveToPublicMusic(context, piece, buffer, extension = ext)
        if (uri != null) {
            val label = if (asMp3) "MP3" else "Studio WAV"
            exportStatusMessage.value = "✔ $label Downloaded to Music/Symphonia/${piece.title}.$ext"
            Toast.makeText(context, "✔ $label downloaded to device Music folder!", Toast.LENGTH_LONG).show()
        } else {
            // Fallback export to cache file
            val file = exportCurrentSong(context, asMp3 = asMp3)
            if (file != null) {
                exportStatusMessage.value = "✔ Audio ready in cache: ${file.name}"
                Toast.makeText(context, "Saved to app audio storage", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save audio file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareCurrentSong(context: Context) {
        val piece = _currentPiece.value ?: return
        var file = lastExportedFile.value
        if (file == null || !file.exists()) {
            file = exportCurrentSong(context, asMp3 = true)
        }
        if (file != null && file.exists()) {
            val shareIntent = AudioExporter.createShareIntent(context, file, piece)
            val chooser = android.content.Intent.createChooser(shareIntent, "Share MP3 Song: ${piece.title}")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "Please generate song audio first", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Note & Instrument Preview ---
    fun playPreviewNote(pitchMidi: Int, instrument: InstrumentType) {
        synthesizer.playPreviewNote(pitchMidi, instrument)
    }

    fun toggleFavorite(pieceId: String) {
        repository.toggleFavorite(pieceId)
        if (_currentPiece.value?.id == pieceId) {
            _currentPiece.value = _currentPiece.value?.let { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        synthesizer.release()
    }
}
