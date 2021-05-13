/*
 * Copyright (c) 2021 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.sqldelight.repositories

import com.raywenderlich.android.sqldelight.db.Database
import com.raywenderlich.android.sqldelight.models.BugDetails
import com.raywenderlich.android.sqldelight.models.BugWithQuantity
import com.raywenderlich.android.sqldelight.models.db.Collection
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.SqlDriver
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Access point to the underlying database.
 * Communicates persistent data streams to the ViewModels,
 * which are in turn connected to an Activity/Fragment.
 */
class DatabaseRepository(driver: SqlDriver) {

    private val database = Database(
        driver = driver,
        collectionAdapter = Collection.Adapter(
            creationTimeAdapter = zonedDateTimeAdapter
        )
    )

    fun listCollections(): Query<Collection> {
        return database.collectionQueries.all()
    }

    fun getCollectionById(collectionId: Long): Query<Collection> {
        return database.collectionQueries.findById(collectionId)
    }

    fun listBugsInCollection(collectionId: Long): Query<BugWithQuantity> {
        return database.inCollectionQueries
            .listBugsInCollection(collectionId) { _, bugId, name, quantity ->
                BugWithQuantity(bugId, name, imageUrl = null, quantity)
            }
    }

    fun getBugById(bugId: Long): Query<BugDetails> {
        return database.bugQueries
            .findById(bugId) { bugId, name, description, size, weight, attack, defense ->
                BugDetails(bugId, name, null, description, size, weight, attack, defense)
            }
    }

    fun addCollection(name: String) {
        database.collectionQueries.insert(
            creationTime = ZonedDateTime.now().withZoneSameInstant(UTC),
            name = name
        )
    }

    fun addBug(
        collectionId: Long,
        name: String,
        description: String?,
        size: String,
        weight: String,
        attack: Int,
        defense: Int,
        quantity: Int
    ) {
        database.bugQueries.transaction {
            database.bugQueries.insert(name, description, size, weight, attack, defense)

            val bugId = database.bugQueries.getLastInsertedId().executeAsOne()

            database.inCollectionQueries.addBugToCollection(
                collectionId = collectionId,
                bugId = bugId,
                quantity = quantity
            )
        }

    }

    fun renameCollection(collectionId: Long, name: String) {
        database.collectionQueries.rename(name, collectionId)
    }

    fun increaseQuantity(collectionId: Long, bugId: Long) {
        database.inCollectionQueries.transaction {
            val quantity = database.inCollectionQueries
                .getQuantity(collectionId, bugId)
                .executeAsOneOrNull()
                ?: return@transaction

            database.inCollectionQueries.updateQuantity(collectionId, bugId, quantity + 1)
        }
    }

    fun decreaseQuantity(collectionId: Long, bugId: Long) {
        database.inCollectionQueries.transaction {
            val quantity = database.inCollectionQueries
                .getQuantity(collectionId, bugId)
                .executeAsOneOrNull()
                ?: return@transaction

            if (quantity == 1) {
                database.bugQueries.deleteById(bugId)
            } else {
                database.inCollectionQueries.updateQuantity(collectionId, bugId, quantity - 1)
            }
        }
    }

    fun deleteCollection(collectionId: Long) {
        database.collectionQueries.deleteById(collectionId)
    }
}

private val UTC = ZoneId.of("UTC")

private val zonedDateTimeAdapter = object : ColumnAdapter<ZonedDateTime, Long> {
    override fun decode(databaseValue: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(databaseValue), UTC)
    }

    override fun encode(value: ZonedDateTime): Long {
        return value.toEpochSecond()
    }

}
