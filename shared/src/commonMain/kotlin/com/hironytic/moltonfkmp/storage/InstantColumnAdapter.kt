package com.hironytic.moltonfkmp.storage

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant

internal object InstantColumnAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}
