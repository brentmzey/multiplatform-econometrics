package org.research.causal.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver? {
        val driver = JdbcSqliteDriver("jdbc:sqlite:polling.db")
        // In real life, check if schema is created, but for now we just try
        try {
            PollingDatabase.Schema.create(driver)
        } catch(e: Exception) {}
        return driver
    }
}
