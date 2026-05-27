package com.example.domain.service

import java.security.MessageDigest

object TokenHasher {
    fun sha256(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
