package com.rateel.app.domain.source

/** Small search-only normalization; display names and stable IDs are never changed. */
object ArabicSearch {
    private val marks = Regex("[\\u064B-\\u065F\\u0670]")
    private val spaces = Regex("\\s+")
    fun normalize(value: String): String = value.lowercase()
        .replace(marks, "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace(spaces, " ").trim()
    fun matches(value: String, query: String): Boolean = normalize(value).contains(normalize(query))
}
