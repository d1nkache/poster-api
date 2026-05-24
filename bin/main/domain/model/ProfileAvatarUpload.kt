package com.example.domain.model

data class ProfileAvatarUpload(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String
)
