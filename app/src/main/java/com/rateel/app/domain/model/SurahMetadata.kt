package com.rateel.app.domain.model

data class SurahMetadata(
    val number: Int,
    val name: String,
    val startPage: Int?,
    val endPage: Int?,
    val isMakki: Boolean?,
)
