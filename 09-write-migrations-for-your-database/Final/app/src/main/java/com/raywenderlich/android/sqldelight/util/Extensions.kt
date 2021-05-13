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

package com.raywenderlich.android.sqldelight.util

import android.content.Context
import android.content.res.Resources
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.raywenderlich.android.sqldelight.App
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.ui.bugdetail.BugDetailActivity
import com.raywenderlich.android.sqldelight.ui.bugdetail.BugDetailViewModel
import com.raywenderlich.android.sqldelight.ui.collectiondetail.CollectionDetailActivity
import com.raywenderlich.android.sqldelight.ui.collectiondetail.CollectionDetailViewModel
import com.raywenderlich.android.sqldelight.ui.collectionlist.CollectionListViewModel
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

/* Context */

val Context.app: App get() = applicationContext as App

/* Resources */

fun Resources.getDateTimeFormatter(@StringRes formatRes: Int): DateTimeFormatter =
  DateTimeFormatter.ofPattern(getString(formatRes))

/**
 * Replacement for a "proper" dependency injection solution.
 * This special version of a ViewModel factory will inject
 * the DatabaseRepository object into the supported ViewModel sub-classes
 * and forward everything else to the default implementation.
 *
 * In a real app, you'll want to use something like Koin, Dagger or Hilt instead.
 */
val ComponentActivity.vmFactory: ViewModelProvider.Factory
  get() = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when (modelClass) {
      CollectionListViewModel::class.java ->
        CollectionListViewModel(repository = app.databaseRepository) as T

      CollectionDetailViewModel::class.java ->
        CollectionDetailViewModel(
          repository = app.databaseRepository,
          collectionId = intent.getLongExtra(CollectionDetailActivity.EXTRA_ID, 0L)
        ) as T

      BugDetailViewModel::class.java ->
        BugDetailViewModel(
          repository = app.databaseRepository,
          bugId = intent.getLongExtra(BugDetailActivity.EXTRA_ID, 0L)
        ) as T

      else -> defaultViewModelProviderFactory.create(modelClass)
    }
  }

/* MaterialDialogAlertBuilder */

/**
 * Add an [EditText] to a dialog in a convenient manner.
 *
 * The given [labelRes] will be shown as its hint text
 * and whenever the user enters some text into it, the [ref] is updated.
 * Use the latter parameter to obtain the entered text in the dialog's
 * button callbacks, for instance.
 */
fun MaterialAlertDialogBuilder.setInput(
  @StringRes labelRes: Int,
  ref: AtomicReference<String>
) = also { builder ->
  builder.setView(TextInputLayout(builder.context).also { layout ->
    layout.addView(TextInputEditText(builder.context).also { editText ->
      editText.setHint(labelRes)
      editText.setText(ref.get())
      editText.setSingleLine()
      editText.doAfterTextChanged { text ->
        ref.set(text?.toString())
      }
    })

    layout.setPadding(builder.context.resources.getDimensionPixelSize(R.dimen.dialog_input_padding))
  })
}
