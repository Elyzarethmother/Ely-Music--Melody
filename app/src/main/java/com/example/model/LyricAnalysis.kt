package com.example.model

data class LyricLineAnalysis(
    val lineNumber: Int,
    val text: String,
    val isSectionHeader: Boolean,
    val sectionTag: String?,
    val syllableCount: Int,
    val rhymeEnding: String?
)

object LyricUtils {

    /**
     * Estimates English syllable count using vowel grouping and phonetic rules.
     */
    fun countSyllablesInWord(word: String): Int {
        val cleaned = word.lowercase().filter { it.isLetter() }
        if (cleaned.isEmpty()) return 0
        if (cleaned.length <= 3) return 1

        val vowels = "aeiou"
        var count = 0
        var prevWasVowel = false

        for (i in cleaned.indices) {
            val c = cleaned[i]
            val isVowel = when (c) {
                'a', 'e', 'i', 'o', 'u' -> true
                'y' -> {
                    // 'y' is a vowel if preceded by a consonant and not followed by a vowel
                    val prevIsConsonant = i > 0 && cleaned[i - 1] !in vowels
                    val nextIsVowel = i + 1 < cleaned.length && cleaned[i + 1] in vowels
                    prevIsConsonant && !nextIsVowel
                }
                else -> false
            }

            if (isVowel && !prevWasVowel) {
                count++
            }
            prevWasVowel = isVowel
        }

        // Handle silent 'e' at the end (e.g., "rose", "grace", "flame")
        if (cleaned.endsWith("e") && !cleaned.endsWith("le") && count > 1) {
            val secondLast = cleaned[cleaned.length - 2]
            if (secondLast !in vowels) {
                count--
            }
        }

        // Handle common word endings
        if (cleaned.endsWith("ed") && !cleaned.endsWith("ted") && !cleaned.endsWith("ded") && count > 1) {
            count--
        }

        return maxOf(1, count)
    }

    fun countSyllablesInLine(line: String): Int {
        val words = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.sumOf { countSyllablesInWord(it) }
    }

    fun extractRhymeEnding(line: String): String? {
        val words = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        val lastWord = words.lastOrNull()?.lowercase()?.filter { it.isLetter() } ?: return null
        if (lastWord.length < 2) return lastWord

        // Last 2 or 3 letters phonetic approximation
        return if (lastWord.length >= 3) lastWord.takeLast(3) else lastWord.takeLast(2)
    }

    fun analyzeLyrics(rawLyrics: String): List<LyricLineAnalysis> {
        val lines = rawLyrics.lines()
        var currentSection: String? = null

        return lines.mapIndexed { index, line ->
            val trimmed = line.trim()
            val isHeader = trimmed.startsWith("[") && trimmed.endsWith("]")

            if (isHeader) {
                currentSection = trimmed
                LyricLineAnalysis(
                    lineNumber = index + 1,
                    text = trimmed,
                    isSectionHeader = true,
                    sectionTag = trimmed,
                    syllableCount = 0,
                    rhymeEnding = null
                )
            } else if (trimmed.isBlank()) {
                LyricLineAnalysis(
                    lineNumber = index + 1,
                    text = "",
                    isSectionHeader = false,
                    sectionTag = currentSection,
                    syllableCount = 0,
                    rhymeEnding = null
                )
            } else {
                val syllables = countSyllablesInLine(trimmed)
                val rhyme = extractRhymeEnding(trimmed)
                LyricLineAnalysis(
                    lineNumber = index + 1,
                    text = trimmed,
                    isSectionHeader = false,
                    sectionTag = currentSection,
                    syllableCount = syllables,
                    rhymeEnding = rhyme
                )
            }
        }
    }
}
