package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiOrchestrator
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

    // Creation Input State (Suno Style)
    val promptInput = MutableStateFlow("An epic cinematic battle theme with driving staccato strings, soaring French horns, and thunderous timpani drops.")
    val isCustomMode = MutableStateFlow(false)
    val customTitleInput = MutableStateFlow("")
    val customLyricsInput = MutableStateFlow("")
    val isInstrumental = MutableStateFlow(false)

    // Style Parameters
    val selectedPreset = MutableStateFlow(StylePresets.ALL[0])
    val tempoBpm = MutableStateFlow(136)
    val keySignature = MutableStateFlow("D Minor")
    val emotionalMood = MutableStateFlow("Heroic & Triumphant")
    val timeSignature = MutableStateFlow("4/4")
    val selectedInstruments = MutableStateFlow<Set<InstrumentType>>(
        setOf(
            InstrumentType.STRING_SECTION,
            InstrumentType.FRENCH_HORN,
            InstrumentType.CELLO,
            InstrumentType.TIMPANI
        )
    )

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
                val pair = orchestrator.generateOrchestralPair(
                    userPrompt = promptInput.value,
                    customTitle = customTitleInput.value.ifBlank { null },
                    customLyrics = if (isInstrumental.value) null else customLyricsInput.value.ifBlank { null },
                    tempoBpm = tempoBpm.value,
                    keySignature = keySignature.value,
                    emotionalMood = emotionalMood.value,
                    timeSignature = timeSignature.value,
                    isInstrumental = isInstrumental.value,
                    selectedInstruments = selectedInstruments.value.toList()
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
