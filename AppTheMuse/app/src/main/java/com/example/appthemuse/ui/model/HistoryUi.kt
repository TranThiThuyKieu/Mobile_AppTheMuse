package com.example.appthemuse.ui.model

import com.google.firebase.Timestamp

data class HistoryUi(
    val book: BookUi,
    val progressPercent: Int,
    val lastReadAt: Timestamp?
)