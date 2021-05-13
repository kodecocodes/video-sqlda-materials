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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.raywenderlich.android.sqldelight.R
import com.raywenderlich.android.sqldelight.models.BugWithQuantity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

private val diffCallback = object : DiffUtil.ItemCallback<BugWithQuantity>() {
  override fun areItemsTheSame(
    oldItem: BugWithQuantity,
    newItem: BugWithQuantity
  ): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(
    oldItem: BugWithQuantity,
    newItem: BugWithQuantity
  ): Boolean {
    return oldItem == newItem
  }
}

class BugWithQuantityAdapter : ListAdapter<BugWithQuantity, BugWithQuantityHolder>(diffCallback) {

  sealed class Event {
    data class Click(val bugId: Long) : Event()
    data class QuantityDown(val bugId: Long) : Event()
    data class QuantityUp(val bugId: Long) : Event()
  }

  private val _clickEvents = Channel<Event>()

  /**
   * Emit clicks on a list item's ID through this hot stream
   */
  val clickEvents: Flow<Event> =
    _clickEvents.receiveAsFlow()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BugWithQuantityHolder(parent)

  override fun onBindViewHolder(holder: BugWithQuantityHolder, position: Int) {
    val item = getItem(position)
    holder.name.text = item.name
    holder.badge.text = item.quantity.toString()

    // Load the bug's image if any URL is given,
    // otherwise use a tinted drawable instead
    if (item.imageUrl != null) {
      holder.image.load(item.imageUrl) {
        crossfade(true)
        placeholder(R.drawable.ic_ladybug_primary)
        fallback(R.drawable.ic_ladybug_primary)
      }
    } else {
      holder.image.load(R.drawable.ic_ladybug_primary)
    }

    holder.layout.setOnClickListener { _clickEvents.offer(Event.Click(item.id)) }
    holder.quantityUp.setOnClickListener { _clickEvents.offer(Event.QuantityUp(item.id)) }
    holder.quantityDown.setOnClickListener { _clickEvents.offer(Event.QuantityDown(item.id)) }
  }
}

class BugWithQuantityHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
  LayoutInflater.from(parent.context).inflate(R.layout.item_bug, parent, false)
) {

  val layout: View = itemView.findViewById(R.id.layout)
  val name: TextView = itemView.findViewById(R.id.name)
  val badge: TextView = itemView.findViewById(R.id.badge)
  val image: ImageView = itemView.findViewById(R.id.image)
  val quantityUp: ImageView = itemView.findViewById(R.id.quantityUp)
  val quantityDown: ImageView = itemView.findViewById(R.id.quantityDown)
}