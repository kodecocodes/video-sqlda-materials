package com.raywenderlich.android.sqldelight.repositories

import com.raywenderlich.android.sqldelight.db.Database
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver.Companion.IN_MEMORY
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseRepositoryTests {

    private val driver = JdbcSqliteDriver(IN_MEMORY).also(Database.Schema::create)

    @Test
    fun `test creating a new collection`() {
        val repository = DatabaseRepository(driver)

        repository.addCollection("New Collection")

        val collections = repository.listCollections().executeAsList()
        assertEquals(1, collections.size)
        assertEquals("New Collection", collections.first().name)
    }
}
