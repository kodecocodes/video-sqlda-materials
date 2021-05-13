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

package com.raywenderlich.android.sqldelight.ui.bugdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.databinding.ActivityBugDetailBinding
import com.raywenderlich.android.sqldelight.ui.bugdetail.BugDetailViewModel.State
import com.raywenderlich.android.sqldelight.util.vmFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BugDetailActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_ID = "bug_id"

    fun newIntent(context: Context, bugId: Long) =
      Intent(context, BugDetailActivity::class.java).also {
        it.putExtra(EXTRA_ID, bugId)
      }
  }

  private val viewModel by viewModels<BugDetailViewModel> { vmFactory }

  private val binding by lazy {
    ActivityBugDetailBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Setup UI
    setContentView(binding.root)

    supportActionBar?.let { toolbar ->
      // Navigation in the toolbar
      toolbar.setDisplayHomeAsUpEnabled(true)
      toolbar.setTitle(R.string.title_bug_details)
    }

    viewModel.state
      .onEach { handleState(it) }
      .launchIn(lifecycleScope)
  }

  override fun onOptionsItemSelected(item: MenuItem) =
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      true
    } else {
      super.onOptionsItemSelected(item)
    }

  /* Private */

  private fun handleState(state: State) =
    when (state) {
      is State.Loading -> {
      }

      is State.Result -> {
        // Apply UI
        val bug = state.bug

        binding.name.text = bug.name
        binding.description.text = bug.description
        binding.size.text = bug.size
        binding.weight.text = bug.weight

        if (bug.imageUrl != null) {
          binding.image.load(bug.imageUrl)
        } else {
          binding.image.load(R.drawable.ic_ladybug_primary)
        }

        binding.stats.text = getString(
          R.string.label_stats_format,
          bug.atk,
          bug.def
        )
      }

      is State.NotFound -> {
        finish()
      }
    }
}
