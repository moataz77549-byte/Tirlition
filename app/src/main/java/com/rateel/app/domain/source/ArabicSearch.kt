package com.rateel.app.domain.source

/** Small search-only normalization; display names and stable IDs are never changed. */
object ArabicSearch {
    private val marks = Regex("[\\u064B-\\u065F\\u0670]")
    private val tatweel = Regex("\\u0640")
    private val spaces = Regex("\\s+")

    fun normalize(value: String): String = value.lowercase()
        .replace(marks, "")
        .replace(tatweel, "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
        .replace('ة', 'ه')
        .replace('ى', 'ي')
        .replace('ؤ', 'و').replace('ئ', 'ي')
        .replace(spaces, " ").trim()

    fun matches(value: String, query: String): Boolean = normalize(value).contains(normalize(query))
}
