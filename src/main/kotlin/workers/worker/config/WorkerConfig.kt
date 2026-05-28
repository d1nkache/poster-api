package com.example.workers.worker.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.yaml.YamlConfig

data class WorkerConfig(
    val database: DatabaseConfig,
    val outboxBatchSize: Int,
    val outboxDelayMs: Long,
    val otpBatchSize: Int,
    val otpDelayMs: Long,
    val imapDelayMs: Long,
    val mail: MailDefaults,
    val authMail: AuthMailConfig
) {
    companion object {
        fun fromResources(path: String? = null): WorkerConfig {
            val config = YamlConfig(path) ?: error("application.yaml was not found")

            return WorkerConfig(
                database = DatabaseConfig.fromConfig(config),
                outboxBatchSize = config.int("worker.outboxBatchSize", 25),
                outboxDelayMs = config.long("worker.outboxDelayMs", 5_000),
                otpBatchSize = config.int("worker.otpBatchSize", 25),
                otpDelayMs = config.long("worker.otpDelayMs", 5_000),
                imapDelayMs = config.long("worker.imapDelayMs", 30_000),
                mail = MailDefaults.fromConfig(config),
                authMail = AuthMailConfig.fromConfig(config)
            )
        }
    }
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val driverClassName: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): DatabaseConfig {
            return DatabaseConfig(
                jdbcUrl = config.string("database.jdbcUrl", "jdbc:postgresql://localhost:5432/posterDb"),
                driverClassName = config.string("database.driverClassName", "org.postgresql.Driver"),
                username = config.string("database.username", "postgres"),
                password = config.string("database.password", "postgres"),
                maximumPoolSize = config.int("database.maximumPoolSize", 5)
            )
        }
    }
}

data class MailDefaults(
    val smtpHost: String?,
    val smtpPort: Int?,
    val imapHost: String?,
    val imapPort: Int?
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): MailDefaults {
            return MailDefaults(
                smtpHost = config.optionalString("mail.defaults.smtpHost"),
                smtpPort = config.optionalInt("mail.defaults.smtpPort"),
                imapHost = config.optionalString("mail.defaults.imapHost"),
                imapPort = config.optionalInt("mail.defaults.imapPort")
            )
        }
    }
}

data class AuthMailConfig(
    val fromEmail: String,
    val username: String,
    val password: String,
    val smtpHost: String,
    val smtpPort: Int
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): AuthMailConfig {
            val username = config.string("mail.auth.username", "")
            val fromEmail = config.string("mail.auth.fromEmail", "")

            return AuthMailConfig(
                fromEmail = fromEmail.ifBlank { username },
                username = username,
                password = config.string("mail.auth.password", ""),
                smtpHost = config.string("mail.auth.smtpHost", ""),
                smtpPort = config.int("mail.auth.smtpPort", 587)
            )
        }
    }

    fun validate() {
        require(fromEmail.isNotBlank()) { "mail.auth.fromEmail or mail.auth.username is required" }
        require(username.isNotBlank()) { "mail.auth.username is required" }
        require(password.isNotBlank()) { "mail.auth.password is required" }
        require(smtpHost.isNotBlank()) { "mail.auth.smtpHost is required" }
    }
}

private fun ApplicationConfig.string(path: String, default: String): String {
    return propertyOrNull(path)?.getString() ?: default
}

private fun ApplicationConfig.optionalString(path: String): String? {
    return propertyOrNull(path)?.getString()?.takeIf { it.isNotBlank() }
}

private fun ApplicationConfig.int(path: String, default: Int): Int {
    return propertyOrNull(path)?.getString()?.toIntOrNull() ?: default
}

private fun ApplicationConfig.optionalInt(path: String): Int? {
    return propertyOrNull(path)?.getString()?.toIntOrNull()
}

private fun ApplicationConfig.long(path: String, default: Long): Long {
    return propertyOrNull(path)?.getString()?.toLongOrNull() ?: default
}
