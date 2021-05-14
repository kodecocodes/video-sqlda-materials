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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.databinding.ActivityCollectionDetailBinding
import com.raywenderlich.android.sqldelight.ui.bugdetail.BugDetailActivity
import com.raywenderlich.android.sqldelight.ui.collectiondetail.BugWithQuantityAdapter.Event
import com.raywenderlich.android.sqldelight.ui.collectiondetail.CollectionDetailViewModel.State
import com.raywenderlich.android.sqldelight.util.getDateTimeFormatter
import com.raywenderlich.android.sqldelight.util.setInput
import com.raywenderlich.android.sqldelight.util.vmFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicReference

class CollectionDetailActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_ID = "collection_id"

    fun newIntent(context: Context, collectionId: Long) =
      Intent(context, CollectionDetailActivity::class.java).also {
        it.putExtra(EXTRA_ID, collectionId)
      }
  }

  private val viewModel by viewModels<CollectionDetailViewModel> { vmFactory }
  private val listAdapter = BugWithQuantityAdapter()

  private val binding by lazy {
    ActivityCollectionDetailBinding.inflate(layoutInflater)
  }

  private val formatter by lazy { resources.getDateTimeFormatter(R.string.timestamp_format) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Setup UI
    setContentView(binding.root)

    supportActionBar?.let { toolbar ->
      // Navigation in the toolbar
      toolbar.setDisplayHomeAsUpEnabled(true)
      toolbar.title = ""
    }

    with(binding.recyclerView) {
      adapter = listAdapter
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    viewModel.state
      .onEach { handleState(it) }
      .launchIn(lifecycleScope)

    listAdapter.clickEvents
      .onEach { handleItemClick(it) }
      .launchIn(lifecycleScope)

    binding.addButton.setOnClickListener {
      showBugCreationDialog()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.collection_detail, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem) =
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }

      R.id.menu_edit -> {
        showEditDialog()
        true
      }

      R.id.menu_delete -> {
        showDeleteDialog()
        true
      }

      else -> super.onOptionsItemSelected(item)
    }

  /* Private */

  private fun handleState(state: State) =
    when (state) {
      is State.Loading -> {
        binding.progressBar.isVisible = true
      }

      is State.Result -> {
        // Keep the toolbar up-to-date with whatever new data comes in
        supportActionBar?.title = resources.getQuantityString(
          /* id = */
          R.plurals.plural_collection_detail_title,
          /* quantity = */
          state.collection.totalBugCount,
          /* formatArgs = */
          state.collection.name, state.collection.totalBugCount,
        )

        // Hold onto the base name of the collection
        // and reuse it later in the Edit Name dialog
        binding.root.tag = state.collection.name

        binding.progressBar.isVisible = false
        binding.description.text = state.collection.description
        binding.createdAt.text = state.collection.createdAt.format(formatter)

        listAdapter.submitList(state.collection.bugs)
      }
    }

  private fun handleItemClick(event: Event) =
    when (event) {
      is Event.Click -> {
        // Open details screen for that bug
        startActivity(BugDetailActivity.newIntent(this, event.bugId))
      }

      is Event.QuantityUp -> {
        viewModel.increaseQuantity(event.bugId)
      }

      is Event.QuantityDown -> {
        viewModel.decreaseQuantity(event.bugId)
      }
    }

  private fun showBugCreationDialog() {
    viewModel.addBug()
  }

  private fun showEditDialog() {
    val ref = AtomicReference<String>(binding.root.tag as? String)

    MaterialAlertDialogBuilder(this)
      .setTitle(R.string.title_rename_collection)
      .setInput(R.string.hint_rename_collection, ref)
      .setPositiveButton(R.string.label_ok) { d, _ ->
        viewModel.updateCollectionName(ref.get())
        d.dismiss()
      }
      .show()
  }

  private fun showDeleteDialog() {
    MaterialAlertDialogBuilder(this)
      .setTitle(R.string.title_delete_collection)
      .setMessage(R.string.text_delete_collection)
      .setPositiveButton(R.string.label_ok) { d, _ ->
        viewModel.deleteCollection()
        d.dismiss()
        finish()
      }
      .setNegativeButton(R.string.label_cancel) { d, _ -> d.dismiss() }
      .show()
  }
}
