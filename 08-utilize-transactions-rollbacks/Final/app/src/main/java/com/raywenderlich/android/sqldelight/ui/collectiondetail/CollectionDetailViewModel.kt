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

package com.raywenderlich.android.sqldelight.ui.collectiondetail

import androidx.lifecycle.ViewModel
import com.raywenderlich.android.sqldelight.models.BugWithQuantity
import com.raywenderlich.android.sqldelight.models.CollectionDetails
import com.raywenderlich.android.sqldelight.repositories.DatabaseRepository
import com.raywenderlich.android.sqldelight.util.nextBug
import com.squareup.sqldelight.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

class CollectionDetailViewModel(
  private val repository: DatabaseRepository,
  private val collectionId: Long,
) : ViewModel() {

  sealed class State {
    object Loading : State()
    data class Result(val collection: CollectionDetails) : State()
  }

  private val _state = MutableStateFlow<State>(State.Loading)
  val state: Flow<State> = _state

  private val collectionQuery = repository.getCollectionById(collectionId)
  private val bugsQuery = repository.listBugsInCollection(collectionId)

  private val listener = object : Query.Listener {
    override fun queryResultsChanged() {
      refreshState()
    }
  }

  init {
    refreshState()

    collectionQuery.addListener(listener)
    bugsQuery.addListener(listener)
  }

  override fun onCleared() {
    collectionQuery.removeListener(listener)
    bugsQuery.removeListener(listener)
  }

  private fun refreshState() {
    collectionQuery.executeAsOneOrNull()?.let { collection ->
      val bugs = bugsQuery.executeAsList()

      _state.value = State.Result(
        collection = CollectionDetails(
          id = collectionId,
          createdAt = collection.creationTime,
          name = collection.name,
          description = "",
          bugs = bugs,
          totalBugCount = bugs.sumBy(BugWithQuantity::quantity),
        )
      )
    }
  }

  fun addBug() {
    // Generate some random data for the bug, then add it
    val bug = Random.nextBug()

    repository.addBug(
      collectionId = collectionId,
      name = bug.name,
      description = bug.description,
      size = bug.size,
      weight = bug.weight,
      attack = bug.atk,
      defense = bug.def,
      quantity = 1
    )
  }

  fun updateCollectionName(newName: String) {
    repository.renameCollection(collectionId, newName)
  }

  fun deleteCollection() {
    repository.deleteCollection(collectionId)
  }

  fun increaseQuantity(bugId: Long) {
    // TODO
  }

  fun decreaseQuantity(bugId: Long) {
    // TODO
  }
}
