package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.audio.InstrumentType
import com.example.audio.MusicalScales
import com.example.audio.NoteEvent
import com.example.audio.TrackLayer
import com.example.audio.TrackType
import com.example.model.OrchestralPiece
import com.example.model.PieceSection
import com.example.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GeminiOrchestrator {

    companion object {
        private const val TAG = "GeminiOrchestrator"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateOrchestralPair(
        userPrompt: String,
        customTitle: String? = null,
        customLyrics: String? = null,
        tempoBpm: Int = 120,
        keySignature: String = "D Minor",
        emotionalMood: String = "Heroic & Triumphant",
        timeSignature: String = "4/4",
        isInstrumental: Boolean = false,
        selectedInstruments: List<InstrumentType> = listOf(
            InstrumentType.STRING_SECTION,
            InstrumentType.FRENCH_HORN,
            InstrumentType.CELLO,
            InstrumentType.TIMPANI
        )
    ): Pair<OrchestralPiece, OrchestralPiece> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidKey) {
            try {
                val aiPieces = callGeminiForPiece(
                    apiKey = apiKey,
                    prompt = userPrompt,
                    customTitle = customTitle,
                    customLyrics = customLyrics,
                    tempoBpm = tempoBpm,
                    keySignature = keySignature,
                    emotionalMood = emotionalMood,
                    timeSignature = timeSignature,
                    isInstrumental = isInstrumental,
                    selectedInstruments = selectedInstruments
                )
                if (aiPieces != null) {
                    return@withContext aiPieces
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to algorithmic music engine", e)
            }
        }

        // High-quality algorithmic synthesis fallback (guarantees instant, musical results)
        val pieceA = generateAlgorithmicPiece(
            seed = Random.nextLong(),
            prompt = userPrompt,
            title = customTitle ?: generateTitleFromPrompt(userPrompt, "Movement I"),
            lyrics = customLyrics,
            tempoBpm = tempoBpm,
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            isInstrumental = isInstrumental,
            selectedInstruments = selectedInstruments,
            variationLabel = "Variation A"
        )

        val pieceB = generateAlgorithmicPiece(
            seed = Random.nextLong(),
            prompt = userPrompt,
            title = customTitle ?: generateTitleFromPrompt(userPrompt, "Movement II"),
            lyrics = customLyrics,
            tempoBpm = tempoBpm + Random.nextInt(-4, 6),
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            isInstrumental = isInstrumental,
            selectedInstruments = selectedInstruments,
            variationLabel = "Variation B"
        )

        Pair(pieceA, pieceB)
    }

    private fun callGeminiForPiece(
        apiKey: String,
        prompt: String,
        customTitle: String?,
        customLyrics: String?,
        tempoBpm: Int,
        keySignature: String,
        emotionalMood: String,
        timeSignature: String,
        isInstrumental: Boolean,
        selectedInstruments: List<InstrumentType>
    ): Pair<OrchestralPiece, OrchestralPiece>? {
        val systemInstruction = """
            You are a master orchestral composer and lyricist (like Suno for orchestral music).
            Given a user's prompt, emotional mood, tempo, and key, return a JSON response containing TWO distinct orchestral variations: 'pieceA' and 'pieceB'.
            Each piece MUST contain:
            - 'title': Majestic orchestral piece title
            - 'styleDescription': Rich description of the arrangement
            - 'lyrics': If not instrumental, provide Suno-style lyrics with tags like [Verse 1], [Chorus], [Orchestral Drop], [Outro].
            - 'melodyNotes': Array of objects { "pitchMidi": Int, "startBeat": Float, "durationBeats": Float } for 16-32 beats.
            - 'chords': Array of chord names e.g. ["Dm", "Bb", "C", "A"]
        """.trimIndent()

        val userMessage = """
            Prompt: $prompt
            Custom Title: ${customTitle ?: "Generate an epic title"}
            Custom Lyrics: ${customLyrics ?: "Generate thematic poetic/epic lyrics"}
            Tempo: $tempoBpm BPM
            Key: $keySignature
            Mood: $emotionalMood
            Time Signature: $timeSignature
            Instrumental: $isInstrumental
            Instruments: ${selectedInstruments.joinToString { it.displayName }}
            
            Respond strictly in valid JSON matching this schema:
            {
              "pieceA": {
                "title": "...",
                "styleDescription": "...",
                "lyrics": "...",
                "melodyNotes": [ { "pitchMidi": 62, "startBeat": 0.0, "durationBeats": 1.0 }, ... ]
              },
              "pieceB": {
                "title": "...",
                "styleDescription": "...",
                "lyrics": "...",
                "melodyNotes": [ { "pitchMidi": 65, "startBeat": 0.0, "durationBeats": 1.0 }, ... ]
              }
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val requestUrl = "$BASE_URL?key=$apiKey"
        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder().url(requestUrl).post(requestBody).build()

        val response = httpClient.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini HTTP error ${response.code}: ${response.message}")
            return null
        }

        val responseBodyString = response.body?.string() ?: return null
        val rootJson = JSONObject(responseBodyString)
        val candidates = rootJson.optJSONArray("candidates") ?: return null
        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        val text = parts.optJSONObject(0)?.optString("text") ?: return null

        val resultJson = JSONObject(text)
        val pieceAJson = resultJson.optJSONObject("pieceA") ?: return null
        val pieceBJson = resultJson.optJSONObject("pieceB") ?: return null

        val pieceA = parseJsonToPiece(
            json = pieceAJson,
            fallbackPrompt = prompt,
            tempoBpm = tempoBpm,
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            isInstrumental = isInstrumental,
            selectedInstruments = selectedInstruments,
            variationLabel = "Variation A"
        )

        val pieceB = parseJsonToPiece(
            json = pieceBJson,
            fallbackPrompt = prompt,
            tempoBpm = tempoBpm,
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            isInstrumental = isInstrumental,
            selectedInstruments = selectedInstruments,
            variationLabel = "Variation B"
        )

        return Pair(pieceA, pieceB)
    }

    private fun parseJsonToPiece(
        json: JSONObject,
        fallbackPrompt: String,
        tempoBpm: Int,
        keySignature: String,
        emotionalMood: String,
        timeSignature: String,
        isInstrumental: Boolean,
        selectedInstruments: List<InstrumentType>,
        variationLabel: String
    ): OrchestralPiece {
        val title = json.optString("title", "Orchestral Movement")
        val styleDesc = json.optString("styleDescription", "Symphonic orchestral composition")
        val lyrics = if (isInstrumental) "" else json.optString("lyrics", "")

        val melodyNotes = mutableListOf<NoteEvent>()
        val notesArray = json.optJSONArray("melodyNotes")
        if (notesArray != null && notesArray.length() > 0) {
            for (i in 0 until notesArray.length()) {
                val obj = notesArray.optJSONObject(i) ?: continue
                val pitch = obj.optInt("pitchMidi", 62)
                val start = obj.optDouble("startBeat", i * 1.0).toFloat()
                val dur = obj.optDouble("durationBeats", 1.0).toFloat()
                melodyNotes.add(NoteEvent(pitchMidi = pitch, startBeat = start, durationBeats = dur))
            }
        }

        // If notes were missing or too short, enrich with algorithmic notes
        val totalBeats = maxOf(32f, melodyNotes.maxOfOrNull { it.startBeat + it.durationBeats } ?: 32f)

        return generateAlgorithmicPiece(
            seed = title.hashCode().toLong(),
            prompt = fallbackPrompt,
            title = title,
            lyrics = lyrics.ifBlank { null },
            tempoBpm = tempoBpm,
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            isInstrumental = isInstrumental,
            selectedInstruments = selectedInstruments,
            variationLabel = variationLabel,
            providedMelody = if (melodyNotes.isNotEmpty()) melodyNotes else null
        )
    }

    fun generateAlgorithmicPiece(
        seed: Long,
        prompt: String,
        title: String,
        lyrics: String? = null,
        tempoBpm: Int = 112,
        keySignature: String = "D Minor",
        emotionalMood: String = "Heroic & Triumphant",
        timeSignature: String = "4/4",
        isInstrumental: Boolean = false,
        selectedInstruments: List<InstrumentType> = listOf(
            InstrumentType.STRING_SECTION,
            InstrumentType.FRENCH_HORN,
            InstrumentType.CELLO,
            InstrumentType.TIMPANI
        ),
        variationLabel: String = "Variation A",
        providedMelody: List<NoteEvent>? = null
    ): OrchestralPiece {
        val rng = Random(seed)
        val rootMidi = MusicalScales.parseRootMidi(keySignature)
        val isMinor = keySignature.contains("Minor", ignoreCase = true) || keySignature.contains("Dorian", ignoreCase = true)
        val scaleIntervals = if (isMinor) MusicalScales.NATURAL_MINOR_INTERVALS else MusicalScales.MAJOR_INTERVALS
        val scalePitches = MusicalScales.getScalePitches(rootMidi, scaleIntervals, octaves = 3)

        val totalBars = 8
        val beatsPerBar = if (timeSignature == "3/4") 3f else if (timeSignature == "6/8") 6f else 4f
        val totalBeats = totalBars * beatsPerBar

        // Generate Lead Melody
        val melodyNotes = providedMelody ?: mutableListOf<NoteEvent>().apply {
            var currentBeat = 0f
            var currentScaleIndex = rng.nextInt(7, 14)

            while (currentBeat < totalBeats) {
                val stepChoices = listOf(0.5f, 1.0f, 1.0f, 1.5f, 2.0f)
                val dur = stepChoices[rng.nextInt(stepChoices.size)].coerceAtMost(totalBeats - currentBeat)
                val pitch = scalePitches[currentScaleIndex.coerceIn(0, scalePitches.lastIndex)]

                add(NoteEvent(pitchMidi = pitch, startBeat = currentBeat, durationBeats = dur, velocity = 0.88f))
                currentBeat += dur

                // Melodic contour motion (stepwise preference with occasional leaps)
                val step = if (rng.nextFloat() < 0.7f) rng.nextInt(-2, 3) else rng.nextInt(-4, 5)
                currentScaleIndex = (currentScaleIndex + step).coerceIn(4, scalePitches.lastIndex)
            }
        }

        // Generate High Harmony (Counter-melody / 3rds & 6ths above)
        val highHarmonyNotes = mutableListOf<NoteEvent>()
        for (m in melodyNotes) {
            // Harmonize 3 or 4 scale steps above (approx 3rd / 4th)
            val harmonyPitch = m.pitchMidi + if (rng.nextBoolean()) 4 else 7
            highHarmonyNotes.add(
                NoteEvent(
                    pitchMidi = harmonyPitch,
                    startBeat = m.startBeat,
                    durationBeats = m.durationBeats,
                    velocity = 0.72f
                )
            )
        }

        // Generate Mid Harmony (Sustained orchestral chords / French Horns & Violas)
        val midHarmonyNotes = mutableListOf<NoteEvent>()
        var bar = 0
        while (bar < totalBars) {
            val startBeat = bar * beatsPerBar
            val dur = beatsPerBar
            // Triad chord tones based on progression
            val chordRoot = rootMidi + when (bar % 4) {
                0 -> 0 // i or I
                1 -> if (isMinor) 8 else 5 // VI or IV
                2 -> if (isMinor) 3 else 7 // III or V
                else -> if (isMinor) 10 else 9 // VII or vi
            }

            midHarmonyNotes.add(NoteEvent(pitchMidi = chordRoot, startBeat = startBeat, durationBeats = dur, velocity = 0.65f))
            midHarmonyNotes.add(NoteEvent(pitchMidi = chordRoot + (if (isMinor) 3 else 4), startBeat = startBeat, durationBeats = dur, velocity = 0.60f))
            midHarmonyNotes.add(NoteEvent(pitchMidi = chordRoot + 7, startBeat = startBeat, durationBeats = dur, velocity = 0.58f))
            bar++
        }

        // Generate Bass Track (Celli / Double Bass / Tuba root movements)
        val bassNotes = mutableListOf<NoteEvent>()
        bar = 0
        while (bar < totalBars) {
            val startBeat = bar * beatsPerBar
            val bassRoot = (rootMidi - 24) + when (bar % 4) {
                0 -> 0
                1 -> if (isMinor) 8 else 5
                2 -> if (isMinor) 3 else 7
                else -> if (isMinor) 10 else 9
            }
            // Driving bass rhythm (half notes or quarter notes)
            val halfBar = beatsPerBar / 2f
            bassNotes.add(NoteEvent(pitchMidi = bassRoot, startBeat = startBeat, durationBeats = halfBar, velocity = 0.85f))
            bassNotes.add(NoteEvent(pitchMidi = bassRoot, startBeat = startBeat + halfBar, durationBeats = halfBar, velocity = 0.80f))
            bar++
        }

        // Generate Percussion Track (Timpani kettle beats and cadence crashes)
        val percNotes = mutableListOf<NoteEvent>()
        bar = 0
        while (bar < totalBars) {
            val startBeat = bar * beatsPerBar
            val timpaniPitch = 36 + (rootMidi % 12) // Low C2 range
            // Heavy downbeat hit
            percNotes.add(NoteEvent(pitchMidi = timpaniPitch, startBeat = startBeat, durationBeats = 1f, velocity = 0.95f))
            if (beatsPerBar >= 4f) {
                // Secondary pulse on beat 3
                percNotes.add(NoteEvent(pitchMidi = timpaniPitch + 7, startBeat = startBeat + 2f, durationBeats = 1f, velocity = 0.80f))
            }
            if (bar == 3 || bar == 7) {
                // Timpani roll leading into next section
                percNotes.add(NoteEvent(pitchMidi = timpaniPitch, startBeat = startBeat + beatsPerBar - 0.75f, durationBeats = 0.25f, velocity = 0.7f))
                percNotes.add(NoteEvent(pitchMidi = timpaniPitch, startBeat = startBeat + beatsPerBar - 0.5f, durationBeats = 0.25f, velocity = 0.8f))
                percNotes.add(NoteEvent(pitchMidi = timpaniPitch, startBeat = startBeat + beatsPerBar - 0.25f, durationBeats = 0.25f, velocity = 0.9f))
            }
            bar++
        }

        // Instrument assignments
        val leadInst = selectedInstruments.firstOrNull { it != InstrumentType.TIMPANI && it != InstrumentType.CELLO }
            ?: InstrumentType.VIOLIN_SOLO
        val highInst = InstrumentType.STRING_SECTION
        val midInst = selectedInstruments.firstOrNull { it == InstrumentType.FRENCH_HORN || it == InstrumentType.CHOIR || it == InstrumentType.HARP }
            ?: InstrumentType.FRENCH_HORN
        val bassInst = InstrumentType.CELLO
        val percInst = InstrumentType.TIMPANI

        val tracks = listOf(
            TrackLayer(
                id = "track_melody",
                type = TrackType.MELODY,
                name = "Lead: ${leadInst.displayName}",
                instrument = leadInst,
                notes = melodyNotes,
                volume = 0.92f
            ),
            TrackLayer(
                id = "track_harmony_high",
                type = TrackType.HIGH_HARMONY,
                name = "High: ${highInst.displayName}",
                instrument = highInst,
                notes = highHarmonyNotes,
                volume = 0.75f
            ),
            TrackLayer(
                id = "track_harmony_mid",
                type = TrackType.MID_HARMONY,
                name = "Chords: ${midInst.displayName}",
                instrument = midInst,
                notes = midHarmonyNotes,
                volume = 0.68f
            ),
            TrackLayer(
                id = "track_bass",
                type = TrackType.BASS,
                name = "Bass: ${bassInst.displayName}",
                instrument = bassInst,
                notes = bassNotes,
                volume = 0.82f
            ),
            TrackLayer(
                id = "track_percussion",
                type = TrackType.PERCUSSION,
                name = "Rhythm: ${percInst.displayName}",
                instrument = percInst,
                notes = percNotes,
                volume = 0.85f
            )
        )

        // Sections
        val sections = listOf(
            PieceSection(
                type = SectionType.INTRO,
                name = "Intro",
                startBeat = 0f,
                lengthBeats = beatsPerBar * 2,
                lyrics = if (isInstrumental) "" else "[Intro]\n(Swelling harp & strings rise with the dawn)"
            ),
            PieceSection(
                type = SectionType.VERSE,
                name = "Verse 1",
                startBeat = beatsPerBar * 2,
                lengthBeats = beatsPerBar * 2,
                lyrics = if (isInstrumental) "" else "[Verse 1]\nThrough the silent ancient halls\nEchoes where the thunder falls"
            ),
            PieceSection(
                type = SectionType.CHORUS,
                name = "Chorus",
                startBeat = beatsPerBar * 4,
                lengthBeats = beatsPerBar * 2,
                lyrics = if (isInstrumental) "" else "[Chorus]\nRise above the storm and flame\nSing the glory of the name"
            ),
            PieceSection(
                type = SectionType.OUTRO,
                name = "Outro",
                startBeat = beatsPerBar * 6,
                lengthBeats = beatsPerBar * 2,
                lyrics = if (isInstrumental) "" else "[Outro]\n(Soli violins fade into starlight)"
            )
        )

        val resolvedLyrics = if (isInstrumental) {
            "[Instrumental Piece - No Vocals]"
        } else {
            lyrics ?: sections.joinToString("\n\n") { it.lyrics }.trim()
        }

        return OrchestralPiece(
            id = UUID.randomUUID().toString(),
            title = title,
            prompt = prompt,
            styleDescription = "$emotionalMood in $keySignature • $tempoBpm BPM • $timeSignature",
            tempoBpm = tempoBpm,
            keySignature = keySignature,
            emotionalMood = emotionalMood,
            timeSignature = timeSignature,
            instrumentationTags = selectedInstruments.map { it.displayName },
            lyrics = resolvedLyrics,
            isInstrumental = isInstrumental,
            sections = sections,
            tracks = tracks,
            totalBeats = totalBeats,
            variationLabel = variationLabel,
            accentColorHex = when (emotionalMood) {
                "Heroic & Triumphant" -> 0xFFF5B74F
                "Melancholic & Poignant" -> 0xFF38BDF8
                "Mystical & Ethereal" -> 0xFFA78BFA
                "Dark & Majestic" -> 0xFFFB7185
                "Romantic & Lush" -> 0xFFF472B6
                else -> 0xFF2DD4BF
            }
        )
    }

    suspend fun generateSophisticatedHarmonies(
        piece: OrchestralPiece,
        style: com.example.model.HarmonicStyle,
        useContraryMotion: Boolean = true,
        useSuspensions: Boolean = true,
        useHarpArpeggios: Boolean = true
    ): Pair<List<TrackLayer>, List<String>> = withContext(Dispatchers.IO) {
        val rootMidi = MusicalScales.parseRootMidi(piece.keySignature)
        val isMinor = piece.keySignature.contains("Minor", ignoreCase = true) || piece.keySignature.contains("Dorian", ignoreCase = true)
        val totalBars = 8
        val beatsPerBar = if (piece.timeSignature == "3/4") 3f else if (piece.timeSignature == "6/8") 6f else 4f
        val totalBeats = totalBars * beatsPerBar

        // Determine bar-by-bar Classical / Romantic Chord Progression
        val chordNames = mutableListOf<String>()
        val chordRoots = mutableListOf<Int>()
        val chordTypes = mutableListOf<String>() // "min", "maj", "dom7", "dim", "maj7", "9"

        when (style) {
            com.example.model.HarmonicStyle.ROMANTIC_BRAHMS -> {
                // Chromatic mediants & Romantic 9th chords: i - bVI - bIII - iv - i - bVI - V9 - i
                if (isMinor) {
                    chordRoots.addAll(listOf(rootMidi, rootMidi + 8, rootMidi + 3, rootMidi + 5, rootMidi, rootMidi + 8, rootMidi + 7, rootMidi))
                    chordTypes.addAll(listOf("min", "maj", "maj", "min", "min", "maj", "dom7", "min"))
                    chordNames.addAll(listOf("i (Tonic)", "♭VI (Chromatic Mediant)", "♭III (Mediant)", "iv (Subdominant)", "i (Tonic)", "♭VI (Lush Mediant)", "V⁹ (Romantic Dominant)", "i (Resolution)"))
                } else {
                    chordRoots.addAll(listOf(rootMidi, rootMidi + 4, rootMidi + 9, rootMidi + 5, rootMidi + 0, rootMidi + 2, rootMidi + 7, rootMidi))
                    chordTypes.addAll(listOf("maj", "min", "min", "maj", "maj", "min", "dom7", "maj"))
                    chordNames.addAll(listOf("I (Tonic)", "iii (Mediant)", "vi (Submediant)", "IV (Subdominant)", "I⁶ (First Inversion)", "ii⁷ (Supertonic)", "V⁹ (Dominant 9th)", "I (Resolution)"))
                }
            }
            com.example.model.HarmonicStyle.CLASSICAL_COUNTERPOINT -> {
                // Strict circle of 5ths & cadential preparation: I - vi - ii - V7 - I - IV - V7 - I
                if (isMinor) {
                    chordRoots.addAll(listOf(rootMidi, rootMidi + 5, rootMidi + 10, rootMidi + 3, rootMidi + 8, rootMidi + 2, rootMidi + 7, rootMidi))
                    chordTypes.addAll(listOf("min", "min", "maj", "maj", "maj", "dim", "dom7", "min"))
                    chordNames.addAll(listOf("i (Tonic)", "iv (Subdominant)", "VII (Subtonic)", "III (Mediant)", "VI (Submediant)", "ii°⁶ (Neapolitan Prep)", "V⁷ (Cadential Dominant)", "i (Resolution)"))
                } else {
                    chordRoots.addAll(listOf(rootMidi, rootMidi + 9, rootMidi + 2, rootMidi + 7, rootMidi, rootMidi + 5, rootMidi + 7, rootMidi))
                    chordTypes.addAll(listOf("maj", "min", "min", "dom7", "maj", "maj", "dom7", "maj"))
                    chordNames.addAll(listOf("I (Tonic)", "vi (Submediant)", "ii (Supertonic)", "V⁷ (Authentic Dominant)", "I (Tonic)", "IV (Subdominant)", "V⁷ (Cadential)", "I (Final Tonic)"))
                }
            }
            com.example.model.HarmonicStyle.ROMANTIC_TCHAIKOVSKY -> {
                // Descending Passacaglia / Lamento Bass: i - i7/7 - IV6 - bVI - i6/4 - V7 - iv - i
                chordRoots.addAll(listOf(rootMidi, rootMidi, rootMidi + 5, rootMidi + (if (isMinor) 8 else 9), rootMidi, rootMidi + 7, rootMidi + 5, rootMidi))
                chordTypes.addAll(listOf("min", "min", "maj", "maj", "min", "dom7", "min", "min"))
                chordNames.addAll(listOf("i (Lamento Head)", "i⁷/♭7 (Descending Bass)", "IV⁶ (Subdominant 6th)", "♭VI (Passionate)", "i⁶₄ (Cadential 6/4)", "V⁷ (Tragic Climax)", "iv (Echo)", "i (Resolution)"))
            }
            com.example.model.HarmonicStyle.HEROIC_BEETHOVEN -> {
                // Dominant pedal & heroic fanfare cadences: I - V6/5 - I - IV - I6/4 - V7 - V7 - I
                chordRoots.addAll(listOf(rootMidi, rootMidi + 7, rootMidi, rootMidi + 5, rootMidi, rootMidi + 7, rootMidi + 7, rootMidi))
                chordTypes.addAll(listOf("maj", "dom7", "maj", "maj", "maj", "dom7", "dom7", "maj"))
                chordNames.addAll(listOf("I (Triumphant)", "V⁶₅ (Driving Dominant)", "I (Declaration)", "IV (Subdominant)", "I⁶₄ (Cadential Pedal)", "V⁷ (Dominant Fanfare)", "V⁷ (Forte Tension)", "I (Tutti Cadence)"))
            }
            com.example.model.HarmonicStyle.IMPRESSIONIST_DEBUSSY -> {
                // Extended 9ths & modal floating: Imaj9 - bVIImaj7 - IVmaj9 - ii9 - bVImaj7 - V11 - Imaj9
                chordRoots.addAll(listOf(rootMidi, rootMidi + 10, rootMidi + 5, rootMidi + 2, rootMidi + 8, rootMidi + 7, rootMidi + 5, rootMidi))
                chordTypes.addAll(listOf("maj7", "maj7", "maj7", "min", "maj7", "dom7", "maj7", "maj7"))
                chordNames.addAll(listOf("Iᵐᵃʲ⁹ (Ethereal)", "♭VIIᵐᵃʲ⁷ (Floating)", "IVᵐᵃʲ⁹ (Luminous)", "ii⁹ (Gentle)", "♭VIᵐᵃʲ⁷ (Mystic)", "V¹¹ (Impressionist)", "IVᵐᵃʲ⁹ (Swell)", "Iᵐᵃʲ⁹ (Resolution)"))
            }
        }

        val leadTrack = piece.tracks.firstOrNull { it.type == com.example.audio.TrackType.MELODY }
        val melodyNotes = leadTrack?.notes ?: emptyList()

        // 1. High Harmony (Violins I / Flute Counterpoint with Contrary Motion & Suspensions)
        val highHarmonyNotes = mutableListOf<NoteEvent>()
        if (melodyNotes.isNotEmpty()) {
            for (idx in melodyNotes.indices) {
                val current = melodyNotes[idx]
                val next = melodyNotes.getOrNull(idx + 1)
                val barIdx = (current.startBeat / beatsPerBar).toInt().coerceIn(0, totalBars - 1)
                val cRoot = chordRoots[barIdx]
                val cType = chordTypes[barIdx]
                val thirdInterval = if (cType == "min" || cType == "dim") 3 else 4

                // Contrary motion calculation:
                val pitch = if (useContraryMotion && next != null) {
                    val delta = next.pitchMidi - current.pitchMidi
                    // When melody goes UP, counterpoint moves DOWN or holds
                    if (delta > 0) {
                        (cRoot + 12 + 7) - 2 // Descend from 5th
                    } else if (delta < 0) {
                        (cRoot + 12 + thirdInterval) + 2 // Ascend into 3rd
                    } else {
                        cRoot + 12 + thirdInterval
                    }
                } else {
                    // Harmonize in consonant 3rds/6ths above
                    current.pitchMidi + if (idx % 2 == 0) 4 else 7
                }

                // Classical Suspension: on strong downbeats of bars 3, 5, 7 hold a 4th and resolve to 3rd
                val isDownbeat = (current.startBeat % beatsPerBar) == 0f
                if (useSuspensions && isDownbeat && (barIdx == 3 || barIdx == 6)) {
                    val suspensionPitch = cRoot + 12 + 5 // 4th
                    val resolutionPitch = cRoot + 12 + thirdInterval // 3rd
                    val halfDur = current.durationBeats / 2f
                    highHarmonyNotes.add(NoteEvent(pitchMidi = suspensionPitch, startBeat = current.startBeat, durationBeats = halfDur, velocity = 0.78f))
                    highHarmonyNotes.add(NoteEvent(pitchMidi = resolutionPitch, startBeat = current.startBeat + halfDur, durationBeats = halfDur, velocity = 0.72f))
                } else {
                    highHarmonyNotes.add(NoteEvent(pitchMidi = pitch.coerceIn(58, 86), startBeat = current.startBeat, durationBeats = current.durationBeats, velocity = 0.75f))
                }
            }
        }

        // 2. Mid Harmony (Rich Orchestral Chords: French Horns, Violas, Strings Ensemble)
        val midHarmonyNotes = mutableListOf<NoteEvent>()
        for (bar in 0 until totalBars) {
            val startBeat = bar * beatsPerBar
            val cRoot = chordRoots[bar]
            val cType = chordTypes[bar]
            val third = cRoot + if (cType == "min" || cType == "dim") 3 else 4
            val fifth = cRoot + if (cType == "dim") 6 else 7
            val seventh = cRoot + if (cType == "maj7") 11 else 10

            // Voice 1 (Alto): Third
            midHarmonyNotes.add(NoteEvent(pitchMidi = third, startBeat = startBeat, durationBeats = beatsPerBar, velocity = 0.65f))
            // Voice 2 (Tenor): Fifth
            midHarmonyNotes.add(NoteEvent(pitchMidi = fifth, startBeat = startBeat, durationBeats = beatsPerBar, velocity = 0.60f))

            // For Romantic 7th/9th extensions:
            if (cType.contains("7") || style == com.example.model.HarmonicStyle.ROMANTIC_BRAHMS) {
                midHarmonyNotes.add(NoteEvent(pitchMidi = seventh, startBeat = startBeat, durationBeats = beatsPerBar, velocity = 0.55f))
            }
        }

        // 3. Bass Foundation (Cellos, Double Bass, Tuba)
        val bassNotes = mutableListOf<NoteEvent>()
        for (bar in 0 until totalBars) {
            val startBeat = bar * beatsPerBar
            val cRoot = chordRoots[bar]
            val bassPitch = (cRoot - 24).coerceIn(36, 52) // C2 to E3 register

            // For Tchaikovsky Lamento style: descending bass steps
            val actualBass = if (style == com.example.model.HarmonicStyle.ROMANTIC_TCHAIKOVSKY) {
                when (bar) {
                    0 -> rootMidi - 24 // 1
                    1 -> (rootMidi - 24) - 2 // b7
                    2 -> (rootMidi - 24) - 3 // 6
                    3 -> (rootMidi - 24) - 4 // b6
                    4 -> (rootMidi - 24) - 5 // 5
                    5 -> (rootMidi - 24) - 5 // 5 (dominant pedal)
                    6 -> (rootMidi - 24) - 7 // 4
                    else -> rootMidi - 24 // 1
                }
            } else {
                bassPitch
            }

            // Rhythmic bass pulse: Half notes or Quarter note drive
            val pulse = beatsPerBar / 2f
            bassNotes.add(NoteEvent(pitchMidi = actualBass, startBeat = startBeat, durationBeats = pulse, velocity = 0.88f))
            bassNotes.add(NoteEvent(pitchMidi = actualBass + if (bar % 2 == 1) 7 else 0, startBeat = startBeat + pulse, durationBeats = pulse, velocity = 0.82f))
        }

        // 4. Percussion / Harp Orchestral Layer
        val percNotes = mutableListOf<NoteEvent>()
        if (useHarpArpeggios || style == com.example.model.HarmonicStyle.IMPRESSIONIST_DEBUSSY) {
            // Glistening concert harp arpeggiation rippling through the chord
            for (bar in 0 until totalBars) {
                val startBeat = bar * beatsPerBar
                val cRoot = chordRoots[bar]
                val third = cRoot + if (chordTypes[bar] == "min") 3 else 4
                val fifth = cRoot + 7
                val octave = cRoot + 12
                val chordPitches = listOf(cRoot + 12, third + 12, fifth + 12, octave + 12)

                val step = 0.5f // Eighth note harp sweeps
                var b = 0f
                var pIdx = 0
                while (b < beatsPerBar) {
                    val p = chordPitches[pIdx % chordPitches.size]
                    percNotes.add(NoteEvent(pitchMidi = p, startBeat = startBeat + b, durationBeats = step, velocity = 0.65f))
                    pIdx++
                    b += step
                }
            }
        } else {
            // Classical / Heroic Timpani Cadences
            for (bar in 0 until totalBars) {
                val startBeat = bar * beatsPerBar
                val timpaniRoot = 36 + (chordRoots[bar] % 12)
                percNotes.add(NoteEvent(pitchMidi = timpaniRoot, startBeat = startBeat, durationBeats = 1.0f, velocity = 0.95f))
                if (bar == 3 || bar == 6 || bar == 7) {
                    // Cadential roll
                    percNotes.add(NoteEvent(pitchMidi = timpaniRoot, startBeat = startBeat + beatsPerBar - 0.5f, durationBeats = 0.25f, velocity = 0.85f))
                    percNotes.add(NoteEvent(pitchMidi = timpaniRoot, startBeat = startBeat + beatsPerBar - 0.25f, durationBeats = 0.25f, velocity = 0.95f))
                }
            }
        }

        // Assemble updated TrackLayers
        val currentTracks = piece.tracks
        val highInst = if (style == com.example.model.HarmonicStyle.ROMANTIC_BRAHMS) InstrumentType.STRING_SECTION else InstrumentType.VIOLIN_SOLO
        val midInst = if (style == com.example.model.HarmonicStyle.HEROIC_BEETHOVEN) InstrumentType.FRENCH_HORN else InstrumentType.STRING_SECTION
        val percInst = if (useHarpArpeggios) InstrumentType.HARP else InstrumentType.TIMPANI

        val updatedTracks = currentTracks.map { track ->
            when (track.type) {
                com.example.audio.TrackType.HIGH_HARMONY -> track.copy(
                    notes = highHarmonyNotes,
                    instrument = highInst,
                    name = "Counter-Melody (${highInst.displayName})"
                )
                com.example.audio.TrackType.MID_HARMONY -> track.copy(
                    notes = midHarmonyNotes,
                    instrument = midInst,
                    name = "Chords (${midInst.displayName})"
                )
                com.example.audio.TrackType.BASS -> track.copy(
                    notes = bassNotes,
                    name = "Bass (${track.instrument.displayName})"
                )
                com.example.audio.TrackType.PERCUSSION -> track.copy(
                    notes = percNotes,
                    instrument = percInst,
                    name = if (useHarpArpeggios) "Arpeggios (${percInst.displayName})" else "Rhythm (${percInst.displayName})"
                )
                else -> track
            }
        }

        Pair(updatedTracks, chordNames)
    }

    suspend fun suggestLyricContinuation(currentLyrics: String, mood: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val promptText = "Given these orchestral song lyrics in mood '$mood':\n$currentLyrics\n" +
                        "Suggest the next 4 poetic lines with rhyme and meter, formatted with a section tag like [Chorus] or [Bridge]."
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", promptText) })
                            })
                        })
                    })
                }
                val req = Request.Builder().url("$BASE_URL?key=$apiKey")
                    .post(json.toString().toRequestBody("application/json".toMediaType())).build()
                val resp = httpClient.newCall(req).execute()
                val text = resp.body?.string()
                if (text != null) {
                    val root = JSONObject(text)
                    val cand = root.optJSONArray("candidates")?.optJSONObject(0)
                    val parts = cand?.optJSONObject("content")?.optJSONArray("parts")
                    val suggested = parts?.optJSONObject(0)?.optString("text")
                    if (!suggested.isNullOrBlank()) return@withContext suggested.trim()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini lyric suggestion fallback", e)
            }
        }

        // Algorithmic poetic fallback
        val stanzas = listOf(
            "[Chorus]\nAcross the skies the banners fly,\nA thousand stars ignite the sky,\nWith courage forged in sacred light,\nWe march beyond the endless night.",
            "[Bridge]\nThough shadows deep may shroud the ground,\nA symphony of hope is found,\nThe trumpets call, the echoes chime,\nDefying darkness, space and time.",
            "[Verse 2]\nBeneath the silver canopy,\nThe cello weeps for what could be,\nYet in the quiet pulse of dawn,\nThe golden dream is never gone."
        )
        stanzas.random()
    }

    private fun generateTitleFromPrompt(prompt: String, defaultSuffix: String): String {
        val keywords = prompt.split(" ").filter { it.length > 3 }.map { it.replace(Regex("[^a-zA-Z]"), "") }
        val prefix = listOf("Symphony", "Overture", "Rhapsody", "Concerto", "Nocturne", "Odyssey", "Chorale").random()
        val suffix = if (keywords.isNotEmpty()) keywords.take(2).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } else defaultSuffix
        return "$prefix of $suffix"
    }
}
