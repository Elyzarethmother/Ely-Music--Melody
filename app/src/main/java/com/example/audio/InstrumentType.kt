package com.example.audio

enum class InstrumentFamily(val displayName: String) {
    STRINGS("Strings"),
    BRASS("Brass"),
    WOODWINDS("Woodwinds"),
    GUITARS("Guitars & Plucks"),
    KEYS_SYNTH("Keys & Synths"),
    BASS("Bass"),
    PERCUSSION("Percussion & Drums"),
    VOICE("Vocals")
}

enum class InstrumentType(
    val id: String,
    val displayName: String,
    val family: InstrumentFamily,
    val description: String,
    val iconName: String
) {
    // Strings
    VIOLIN_SOLO(
        id = "violin_solo",
        displayName = "Violin Solo",
        family = InstrumentFamily.STRINGS,
        description = "Expressive high bowed strings with lyrical vibrato",
        iconName = "violin"
    ),
    STRING_SECTION(
        id = "string_section",
        displayName = "Strings Ensemble",
        family = InstrumentFamily.STRINGS,
        description = "Lush symphonic string orchestra with rich chorus",
        iconName = "ensemble"
    ),
    CELLO(
        id = "cello",
        displayName = "Cello & Bass",
        family = InstrumentFamily.STRINGS,
        description = "Deep warm resonant bass and low counter-harmonies",
        iconName = "cello"
    ),

    // Guitars & Plucked
    ACOUSTIC_GUITAR(
        id = "acoustic_guitar",
        displayName = "Fingerpicked Acoustic Guitar",
        family = InstrumentFamily.GUITARS,
        description = "Warm resonant acoustic guitar fingerpicking and strums",
        iconName = "guitar"
    ),
    ELECTRIC_GUITAR(
        id = "electric_guitar",
        displayName = "Clean Electric Guitar",
        family = InstrumentFamily.GUITARS,
        description = "Warm clean electric guitar with subtle chime and fills",
        iconName = "electric_guitar"
    ),
    SITAR(
        id = "sitar",
        displayName = "Sitar & Tanpura",
        family = InstrumentFamily.GUITARS,
        description = "Classical Indian plucked strings with resonant sympathetic buzzing",
        iconName = "sitar"
    ),
    HARP(
        id = "harp",
        displayName = "Concert Harp",
        family = InstrumentFamily.GUITARS,
        description = "Glistening plucked arpeggios and ethereal bell tones",
        iconName = "harp"
    ),

    // Keys & Synths
    ELECTRIC_PIANO(
        id = "electric_piano",
        displayName = "Rhodes Electric Piano",
        family = InstrumentFamily.KEYS_SYNTH,
        description = "Warm bell-tine Rhodes tone with gentle tremolo",
        iconName = "electric_piano"
    ),
    GRAND_PIANO(
        id = "grand_piano",
        displayName = "Grand Piano",
        family = InstrumentFamily.KEYS_SYNTH,
        description = "Concert grand acoustic piano with expressive touch",
        iconName = "piano"
    ),
    HARMONIUM(
        id = "harmonium",
        displayName = "Harmonium / Organ",
        family = InstrumentFamily.KEYS_SYNTH,
        description = "Warm reed bellows pump organ with sustained devotional drone",
        iconName = "organ"
    ),
    ANALOG_PAD(
        id = "analog_pad",
        displayName = "Warm Analog Pad",
        family = InstrumentFamily.KEYS_SYNTH,
        description = "Lush vintage polyphonic analog synthesizer pad",
        iconName = "synth"
    ),

    // Bass
    ROUNDED_BASS(
        id = "rounded_bass",
        displayName = "Rounded Bass Guitar",
        family = InstrumentFamily.BASS,
        description = "Warm, rounded electric bass with deep smooth low end",
        iconName = "bass"
    ),
    SUB_BASS(
        id = "sub_bass",
        displayName = "Deep Sub-Bass",
        family = InstrumentFamily.BASS,
        description = "Heavy electronic 808/sub-bass with punchy foundation",
        iconName = "sub"
    ),

    // Woodwinds & Brass
    FRENCH_HORN(
        id = "french_horn",
        displayName = "French Horn",
        family = InstrumentFamily.BRASS,
        description = "Mellow, noble cinematic brass with golden warmth",
        iconName = "horn"
    ),
    TRUMPET(
        id = "trumpet",
        displayName = "Brass Fanfare",
        family = InstrumentFamily.BRASS,
        description = "Bright, triumphant orchestral brass declaration",
        iconName = "trumpet"
    ),
    FLUTE(
        id = "flute",
        displayName = "Concert Flute",
        family = InstrumentFamily.WOODWINDS,
        description = "Airy, graceful woodwind melody with breathy harmonics",
        iconName = "flute"
    ),
    BANSURI(
        id = "bansuri",
        displayName = "Bansuri Bamboo Flute",
        family = InstrumentFamily.WOODWINDS,
        description = "Soulful Indian bamboo flute with microtonal glides and breath",
        iconName = "bansuri"
    ),
    OBOE(
        id = "oboe",
        displayName = "Oboe Solo",
        family = InstrumentFamily.WOODWINDS,
        description = "Penetrating, plaintive classical reed voice",
        iconName = "oboe"
    ),

    // Percussion & Drums
    BRUSHED_DRUMS(
        id = "brushed_drums",
        displayName = "Brushed Drums & Groove",
        family = InstrumentFamily.PERCUSSION,
        description = "Intimate brushed snare, soft kick, and gentle ride",
        iconName = "drums"
    ),
    TABLA_PERCUSSION(
        id = "tabla_percussion",
        displayName = "Tabla & Hand Percussion",
        family = InstrumentFamily.PERCUSSION,
        description = "Classical Indian bayan bass thud and resonant dayan bell tones",
        iconName = "tabla"
    ),
    LATIN_PERCUSSION(
        id = "latin_percussion",
        displayName = "Congas & Dembow Rhythm",
        family = InstrumentFamily.PERCUSSION,
        description = "Organic congas, bongos, shakers, and syncopated beats",
        iconName = "congas"
    ),
    TIMPANI(
        id = "timpani",
        displayName = "Timpani & Percussion",
        family = InstrumentFamily.PERCUSSION,
        description = "Thunderous kettle drums and cinematic crashes",
        iconName = "timpani"
    ),

    // Vocals
    LEAD_VOCAL(
        id = "lead_vocal",
        displayName = "Lead Vocal",
        family = InstrumentFamily.VOICE,
        description = "Expressive vocal singing line with smooth melodic vibrato",
        iconName = "mic"
    ),
    VOCAL_DUET(
        id = "vocal_duet",
        displayName = "Duet: Call & Response",
        family = InstrumentFamily.VOICE,
        description = "Male-female vocal harmony with gentle alternating phrasing",
        iconName = "duet"
    ),
    CHOIR(
        id = "choir",
        displayName = "Epic Choir",
        family = InstrumentFamily.VOICE,
        description = "Symphonic vocal chorus with soaring vowel formants",
        iconName = "choir"
    )
}
