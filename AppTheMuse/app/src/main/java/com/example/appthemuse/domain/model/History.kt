package com.example.appthemuse.domain.model

import com.google.firebase.Timestamp

data class History(
    val book: Book,
    val progressPercent: Int,
    val lastReadAt: Timestamp?
)
