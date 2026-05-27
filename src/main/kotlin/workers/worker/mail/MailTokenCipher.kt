package com.example.workers.worker.mail

interface MailTokenCipher {
    fun decrypt(encryptedToken: String): String
}

class PlainTextMailTokenCipher : MailTokenCipher {
    override fun decrypt(encryptedToken: String): String {
        return encryptedToken
    }
}
