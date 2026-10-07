package com.example.model

enum class HarmonicStyle(
    val id: String,
    val title: String,
    val era: String,
    val description: String,
    val characteristicProgressions: String,
    val tags: List<String>
) {
    ROMANTIC_BRAHMS(
        id = "romantic_brahms",
        title = "Romantic Mediant Swell",
        era = "Late Romantic (Brahms / Rachmaninoff)",
        description = "Lush chromatic mediant shifts, rich 9th suspensions, and passionate contrary-motion counterpoint in celli and horns.",
        characteristicProgressions = "i — ♭VI — ♭III — iv — V⁹ — i",
        tags = listOf("Mediant Shifts", "9th Chords", "Celli Counterpoint", "Lush")
    ),
    CLASSICAL_COUNTERPOINT(
        id = "classical_counterpoint",
        title = "Classical Viennese Counterpoint",
        era = "High Classical (Mozart / Haydn)",
        description = "Strict voice-leading with contrary motion in Violins, circle-of-fifths root motion, and authentic cadential suspensions.",
        characteristicProgressions = "I — vi — ii — V⁷ — I (or i — iv — vii°/V — V — i)",
        tags = listOf("Contrary Motion", "Circle of 5ths", "Suspensions", "Balanced")
    ),
    ROMANTIC_TCHAIKOVSKY(
        id = "romantic_tchaikovsky",
        title = "Romantic Pathétique",
        era = "Russian Romantic (Tchaikovsky)",
        description = "Descending lamento bass line (passacaglia), weeping woodwind counter-melodies, and tragic brass crescendos.",
        characteristicProgressions = "i — i⁷/7 — IV⁶ — V⁷ — i (Descending Bass)",
        tags = listOf("Descending Bass", "Poignant 7ths", "French Horn Swells", "Dramatic")
    ),
    HEROIC_BEETHOVEN(
        id = "heroic_beethoven",
        title = "Heroic Orchestral Fanfare",
        era = "Classical / Early Romantic (Beethoven)",
        description = "Driving dominant pedal points, staccato brass chords, and triumphant tonic-dominant structural declarations.",
        characteristicProgressions = "I — V⁶/₄ — V⁷ — I — IV — I⁶ — V — I",
        tags = listOf("Pedal Point", "Brass Chords", "Triumphant", "Driving")
    ),
    IMPRESSIONIST_DEBUSSY(
        id = "impressionist_debussy",
        title = "Impressionist Ethereal",
        era = "Late 19th c. (Debussy / Ravel)",
        description = "Extended 9th and 11th sonorities, parallel fifth colorings, and shimmering concert harp arpeggios.",
        characteristicProgressions = "Iᵐᵃʲ⁹ — ♭VIIᵐᵃʲ⁷ — IVᵐᵃʲ⁹ — Iᵐᵃʲ⁹",
        tags = listOf("Extended Chords", "Harp Arpeggios", "Ethereal", "Floating")
    )
}
