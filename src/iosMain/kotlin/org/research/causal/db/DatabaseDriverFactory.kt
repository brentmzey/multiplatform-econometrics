package org.research.causal.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver? {
        return NativeSqliteDriver(PollingDatabase.Schema, "polling.db")
    }
}
