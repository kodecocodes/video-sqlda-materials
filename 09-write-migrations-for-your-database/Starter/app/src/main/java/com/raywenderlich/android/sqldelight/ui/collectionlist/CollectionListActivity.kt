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

package com.raywenderlich.android.sqldelight.ui.collectionlist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.databinding.ActivityCollectionListBinding
import com.raywenderlich.android.sqldelight.ui.collectiondetail.CollectionDetailActivity
import com.raywenderlich.android.sqldelight.ui.collectionlist.CollectionListViewModel.State
import com.raywenderlich.android.sqldelight.util.setInput
import com.raywenderlich.android.sqldelight.util.vmFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicReference

class CollectionListActivity : AppCompatActivity() {

  private val viewModel by viewModels<CollectionListViewModel> { vmFactory }
  private val listAdapter = CollectionListAdapter()

  private val binding by lazy {
    ActivityCollectionListBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // Hide splash screen
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)

    // Setup UI
    setContentView(binding.root)

    supportActionBar?.let { toolbar ->
      // Icon in the toolbar
      toolbar.setDisplayHomeAsUpEnabled(true)
      toolbar.setHomeAsUpIndicator(R.drawable.ic_ladybug_white)
    }

    with(binding.list) {
      adapter = listAdapter
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }

    // Observe changes to the persisted data
    viewModel.state
      .onEach { handleState(it) }
      .launchIn(lifecycleScope)

    // Observe clicks on list items
    listAdapter.clickEvents
      .onEach { showCollectionDetails(it) }
      .launchIn(lifecycleScope)

    // Show a dialog and create a new collection upon confirming that dialog
    binding.addButton.setOnClickListener {
      showCollectionCreationDialog()
    }
  }

  /* Private */

  private fun handleState(state: State) =
    when (state) {
      is State.Loading -> {
        binding.progressBar.isVisible = true
      }

      is State.Result -> {
        binding.progressBar.isVisible = false
        listAdapter.submitList(state.collections)
      }
    }

  private fun showCollectionCreationDialog() {
    val nameRef = AtomicReference<String>()

    MaterialAlertDialogBuilder(this@CollectionListActivity)
      .setTitle(R.string.title_add_collection)
      .setInput(R.string.hint_add_collection, nameRef)
      .setPositiveButton(R.string.label_create) { _, _ ->
        val name = nameRef.get() ?: getString(R.string.default_collection_name)
        viewModel.addCollection(name)
      }
      .show()
  }

  private fun showCollectionDetails(id: Long) {
    // Go to the next screen
    startActivity(CollectionDetailActivity.newIntent(this, id))
  }
}
