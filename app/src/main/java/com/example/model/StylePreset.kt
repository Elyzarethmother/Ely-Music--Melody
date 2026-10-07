package com.example.model

import com.example.audio.InstrumentType

data class ProductionConfig(
    val tapeDelay: String,
    val reverb: String,
    val mix: String
)

data class StylePreset(
    val id: String,
    val name: String,
    val bpmRange: Pair<Int, Int>,
    val defaultBpm: Int,
    val mood: String,
    val groove: String,
    val coreInstruments: List<String>,
    val supportingInstruments: List<String>,
    val vocalCharacter: String,
    val production: ProductionConfig,
    val isDuet: Boolean = false,
    val mappedInstruments: List<InstrumentType> = emptyList(),
    val keySignature: String = "D Minor",
    val timeSignature: String = "4/4"
) {
    // Compatibility accessors
    val title: String get() = name
    val subtitle: String get() = "$groove • ${defaultBpm} BPM"
    val tempoBpm: Int get() = defaultBpm
    val emotionalMood: String get() = mood
    val primaryInstruments: List<InstrumentType> get() = mappedInstruments
    val tags: List<String> get() = listOf(groove) + coreInstruments.take(3)
    val defaultPrompt: String get() = "$name style ($groove, ${defaultBpm} BPM). Mood: $mood. Instruments: ${coreInstruments.take(4).joinToString(", ")}."
}

object StylePresets {
    val ALL = listOf(
        StylePreset(
            id = "modern_country_soul",
            name = "Modern Country Soul",
            bpmRange = Pair(78, 92),
            defaultBpm = 82,
            mood = "intimate, warm, nostalgic, tender",
            groove = "slow-dance sway",
            coreInstruments = listOf(
                "fingerpicked acoustic guitar",
                "warm electric piano",
                "rounded bass",
                "brushed drums",
                "hand percussion",
                "clean electric guitar",
                "warm analog pads"
            ),
            supportingInstruments = listOf(
                "pedal steel",
                "soft organ",
                "subtle acoustic guitar strums"
            ),
            vocalCharacter = "natural male-female duet with gentle call and response",
            production = ProductionConfig(
                tapeDelay = "subtle",
                reverb = "soft plate",
                mix = "clean, intimate, organic"
            ),
            isDuet = true,
            mappedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.ANALOG_PAD
            ),
            keySignature = "G Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "emotional_pop_ballad",
            name = "Emotional Pop Ballad",
            bpmRange = Pair(68, 82),
            defaultBpm = 74,
            mood = "emotional, reflective, cinematic",
            groove = "slow contemporary ballad",
            coreInstruments = listOf(
                "grand piano",
                "acoustic guitar",
                "warm strings",
                "electric piano",
                "soft bass",
                "restrained drums"
            ),
            supportingInstruments = listOf(
                "cello",
                "ambient electric guitar",
                "warm synth pads"
            ),
            vocalCharacter = "intimate lead vocal with restrained harmonies",
            production = ProductionConfig(
                tapeDelay = "subtle",
                reverb = "warm plate",
                mix = "wide but vocal-focused"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.GRAND_PIANO,
                InstrumentType.STRING_SECTION,
                InstrumentType.CELLO,
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ROUNDED_BASS
            ),
            keySignature = "C Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "contemporary_rnb_soul",
            name = "Contemporary R&B / Soul Pop",
            bpmRange = Pair(72, 92),
            defaultBpm = 82,
            mood = "smooth, intimate, soulful",
            groove = "laid-back pocket",
            coreInstruments = listOf(
                "Rhodes electric piano",
                "muted electric guitar",
                "warm synth bass",
                "soft kick",
                "snare",
                "finger snaps",
                "shaker"
            ),
            supportingInstruments = listOf(
                "Hammond organ",
                "atmospheric pads",
                "subtle synth lead"
            ),
            vocalCharacter = "close, expressive soul vocal with layered harmonies",
            production = ProductionConfig(
                tapeDelay = "short",
                reverb = "soft room/plate",
                mix = "warm and spacious"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.ELECTRIC_GUITAR,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.ANALOG_PAD
            ),
            keySignature = "E-Flat Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "latin_pop",
            name = "Latin Pop",
            bpmRange = Pair(92, 108),
            defaultBpm = 100,
            mood = "warm, romantic, rhythmic",
            groove = "organic Latin pop",
            coreInstruments = listOf(
                "nylon-string guitar",
                "acoustic piano",
                "bass guitar",
                "congas",
                "bongos",
                "claves",
                "shakers"
            ),
            supportingInstruments = listOf(
                "clean electric guitar",
                "brass accents",
                "hand percussion"
            ),
            vocalCharacter = "melodic lead with conversational backing responses",
            production = ProductionConfig(
                tapeDelay = "light",
                reverb = "short plate",
                mix = "bright and rhythmic"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.GRAND_PIANO,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.LATIN_PERCUSSION,
                InstrumentType.TRUMPET
            ),
            keySignature = "A Minor",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "reggaeton_pop",
            name = "Reggaeton Pop",
            bpmRange = Pair(88, 102),
            defaultBpm = 96,
            mood = "confident, modern, energetic",
            groove = "dembow",
            coreInstruments = listOf(
                "nylon guitar",
                "deep sub-bass",
                "kick",
                "snare/clap",
                "hi-hats",
                "Latin percussion"
            ),
            supportingInstruments = listOf(
                "synth plucks",
                "atmospheric pads",
                "brass/synth stabs"
            ),
            vocalCharacter = "rhythmic lead with melodic pop hook",
            production = ProductionConfig(
                tapeDelay = "minimal",
                reverb = "controlled",
                mix = "tight low end, punchy rhythm"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.SUB_BASS,
                InstrumentType.LATIN_PERCUSSION,
                InstrumentType.ANALOG_PAD
            ),
            keySignature = "F Minor",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "global_dance_kpop",
            name = "Global Dance Pop / K-Pop",
            bpmRange = Pair(105, 125),
            defaultBpm = 116,
            mood = "bright, energetic, polished",
            groove = "four-on-the-floor dance pop",
            coreInstruments = listOf(
                "synth bass",
                "electronic drums",
                "piano",
                "bright synths",
                "synth plucks",
                "layered pads"
            ),
            supportingInstruments = listOf(
                "electric guitar accents",
                "strings",
                "percussion",
                "transition FX"
            ),
            vocalCharacter = "dynamic lead with layered pop harmonies",
            production = ProductionConfig(
                tapeDelay = "tempo-synced accents",
                reverb = "clean digital/plate",
                mix = "wide, punchy, highly controlled"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.SUB_BASS,
                InstrumentType.GRAND_PIANO,
                InstrumentType.ANALOG_PAD,
                InstrumentType.STRING_SECTION,
                InstrumentType.BRUSHED_DRUMS
            ),
            keySignature = "D Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "cinematic_emotional",
            name = "Cinematic Emotional",
            bpmRange = Pair(70, 95),
            defaultBpm = 82,
            mood = "moving, expansive, dramatic",
            groove = "slow cinematic pulse",
            coreInstruments = listOf(
                "grand piano",
                "cinematic strings",
                "cello",
                "French horns",
                "soft percussion",
                "orchestral pads"
            ),
            supportingInstruments = listOf(
                "timpani",
                "acoustic guitar",
                "orchestral swells"
            ),
            vocalCharacter = "emotional lead or instrumental focus",
            production = ProductionConfig(
                tapeDelay = "subtle",
                reverb = "large hall",
                mix = "wide cinematic depth"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.GRAND_PIANO,
                InstrumentType.STRING_SECTION,
                InstrumentType.CELLO,
                InstrumentType.FRENCH_HORN,
                InstrumentType.TIMPANI
            ),
            keySignature = "D Minor",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "acoustic_folk_indie",
            name = "Acoustic Folk / Indie",
            bpmRange = Pair(75, 105),
            defaultBpm = 88,
            mood = "organic, intimate, hopeful",
            groove = "human live-band feel",
            coreInstruments = listOf(
                "fingerpicked acoustic guitar",
                "strummed acoustic guitar",
                "upright bass",
                "brushed drums",
                "hand percussion"
            ),
            supportingInstruments = listOf(
                "mandolin",
                "banjo",
                "harmonica",
                "fiddle"
            ),
            vocalCharacter = "natural close-mic vocal",
            production = ProductionConfig(
                tapeDelay = "very subtle",
                reverb = "small room",
                mix = "organic and uncluttered"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS,
                InstrumentType.VIOLIN_SOLO
            ),
            keySignature = "G Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "blues_rock_southern_soul",
            name = "Blues Rock / Southern Soul",
            bpmRange = Pair(75, 100),
            defaultBpm = 88,
            mood = "earthy, soulful, confident",
            groove = "laid-back live band",
            coreInstruments = listOf(
                "electric guitar",
                "Hammond organ",
                "acoustic guitar",
                "bass guitar",
                "live drums"
            ),
            supportingInstruments = listOf(
                "slide guitar",
                "piano",
                "tambourine",
                "hand percussion"
            ),
            vocalCharacter = "gritty soulful lead with backing responses",
            production = ProductionConfig(
                tapeDelay = "short guitar echoes",
                reverb = "spring/plate",
                mix = "warm analog character"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ELECTRIC_GUITAR,
                InstrumentType.HARMONIUM,
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ROUNDED_BASS,
                InstrumentType.BRUSHED_DRUMS
            ),
            keySignature = "E Major",
            timeSignature = "4/4"
        ),
        StylePreset(
            id = "indian_fusion_soul",
            name = "Indian Fusion / Soul",
            bpmRange = Pair(72, 100),
            defaultBpm = 84,
            mood = "warm, reflective, soulful, atmospheric",
            groove = "organic fusion pulse",
            coreInstruments = listOf(
                "acoustic guitar",
                "electric piano",
                "harmonium",
                "bansuri",
                "sitar",
                "tabla",
                "bass guitar",
                "organic drums"
            ),
            supportingInstruments = listOf(
                "dholak",
                "tanpura/drone",
                "warm synth pads",
                "hand percussion"
            ),
            vocalCharacter = "expressive lead with Indian melodic ornamentation and gentle harmonies",
            production = ProductionConfig(
                tapeDelay = "subtle",
                reverb = "warm plate/hall",
                mix = "organic fusion with clear vocal center"
            ),
            isDuet = false,
            mappedInstruments = listOf(
                InstrumentType.ACOUSTIC_GUITAR,
                InstrumentType.ELECTRIC_PIANO,
                InstrumentType.HARMONIUM,
                InstrumentType.BANSURI,
                InstrumentType.SITAR,
                InstrumentType.TABLA_PERCUSSION,
                InstrumentType.ROUNDED_BASS
            ),
            keySignature = "D Dorian",
            timeSignature = "4/4"
        )
    )

    val MOODS = listOf(
        "intimate, warm, nostalgic, tender",
        "emotional, reflective, cinematic",
        "smooth, intimate, soulful",
        "warm, romantic, rhythmic",
        "confident, modern, energetic",
        "bright, energetic, polished",
        "moving, expansive, dramatic",
        "organic, intimate, hopeful",
        "earthy, soulful, confident",
        "warm, reflective, soulful, atmospheric",
        "Heroic & Triumphant",
        "Melancholic & Poignant",
        "Mystical & Ethereal"
    )

    val KEYS = listOf(
        "G Major", "C Major", "D Major", "A Major", "E Major", "E-Flat Major", "F Major",
        "A Minor", "E Minor", "D Minor", "G Minor", "F Minor", "C Minor", "D Dorian"
    )

    val TIME_SIGNATURES = listOf("4/4", "3/4", "6/8")
}
