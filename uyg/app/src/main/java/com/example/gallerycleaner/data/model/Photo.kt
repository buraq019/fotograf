package com.example.gallerycleaner.data.model

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val size: Long
)
