package com.example.workers.worker.config

data class WorkerConfig(
    val database: DatabaseConfig,
    val outboxBatchSize: Int,
    val outboxDelayMs: Long,
    val imapDelayMs: Long,
    val mail: MailDefaults
) {
    companion object {
        fun fromEnvironment(): WorkerConfig {
            return WorkerConfig(
                database = DatabaseConfig.fromEnvironment(),
                outboxBatchSize = envInt("WORKER_OUTBOX_BATCH_SIZE", 25),
                outboxDelayMs = envLong("WORKER_OUTBOX_DELAY_MS", 5_000),
                imapDelayMs = envLong("WORKER_IMAP_DELAY_MS", 30_000),
                mail = MailDefaults.fromEnvironment()
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
        fun fromEnvironment(): DatabaseConfig {
            return DatabaseConfig(
                jdbcUrl = env("POSTER_DB_JDBC_URL", "jdbc:postgresql://localhost:5432/posterDb"),
                driverClassName = env("POSTER_DB_DRIVER", "org.postgresql.Driver"),
                username = env("POSTER_DB_USERNAME", "postgres"),
                password = env("POSTER_DB_PASSWORD", "postgres"),
                maximumPoolSize = envInt("POSTER_DB_MAX_POOL_SIZE", 5)
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
        fun fromEnvironment(): MailDefaults {
            return MailDefaults(
                smtpHost = System.getenv("POSTER_SMTP_HOST"),
                smtpPort = System.getenv("POSTER_SMTP_PORT")?.toIntOrNull(),
                imapHost = System.getenv("POSTER_IMAP_HOST"),
                imapPort = System.getenv("POSTER_IMAP_PORT")?.toIntOrNull()
            )
        }
    }
}

private fun env(name: String, default: String): String {
    return System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
}

private fun envInt(name: String, default: Int): Int {
    return System.getenv(name)?.toIntOrNull() ?: default
}

private fun envLong(name: String, default: Long): Long {
    return System.getenv(name)?.toLongOrNull() ?: default
}
