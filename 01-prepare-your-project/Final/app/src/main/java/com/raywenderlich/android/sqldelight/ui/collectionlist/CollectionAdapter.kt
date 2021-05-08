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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.models.db.Collection
import com.raywenderlich.android.sqldelight.util.getDateTimeFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.format.DateTimeFormatter

private val diffCallback = object : DiffUtil.ItemCallback<Collection>() {
  override fun areItemsTheSame(
    oldItem: Collection,
    newItem: Collection
  ): Boolean {
    return oldItem.collectionId == newItem.collectionId
  }

  override fun areContentsTheSame(
    oldItem: Collection,
    newItem: Collection
  ): Boolean {
    return oldItem == newItem
  }
}

class CollectionListAdapter : ListAdapter<Collection, CollectionHolder>(diffCallback) {

  private var formatter: DateTimeFormatter? = null
  private val _clickEvents = Channel<Long>()

  /**
   * Emit clicks on a list item's ID through this hot stream
   */
  val clickEvents: Flow<Long> =
    _clickEvents.receiveAsFlow()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CollectionHolder(parent)

  override fun onBindViewHolder(holder: CollectionHolder, position: Int) {
    // Ensure a formatter is available to format the collection's timestamp.
    // Create the object when the first ViewHolder is being bound
    val formatter = this.formatter ?: holder.itemView.resources
      .getDateTimeFormatter(R.string.timestamp_format)
      .also { this.formatter = it }

    // Style
    val item = getItem(position)
    holder.idTextView.text = "#${item.collectionId}"
    holder.nameTextView.text = item.name
    holder.creationTextView.text = item.creationTime.toString()

    // Listeners
    holder.layout.setOnClickListener { _clickEvents.offer(item.collectionId) }
  }
}

class CollectionHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
  LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
) {

  val layout: View = itemView.findViewById(R.id.layout)
  val idTextView: TextView = itemView.findViewById(R.id.collectionId)
  val nameTextView: TextView = itemView.findViewById(R.id.collectionName)
  val creationTextView: TextView = itemView.findViewById(R.id.collectionCreation)
}
