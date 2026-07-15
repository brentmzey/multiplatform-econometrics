package org.research.causal.db

import app.cash.sqldelight.db.SqlDriver

class WasmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver? {
        return null // We will use in-memory cache for WasmJS for now
    }
}
