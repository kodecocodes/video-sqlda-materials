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

import com.raywenderlich.android.sqldelight.models.BugDetails
import kotlin.random.Random

/**
 * Utility function for generating some fun bugs.
 */
fun Random.nextBug() =
  BugDetails(
    id = 0L,
    name = randomBugName(),
    imageUrl = randomUrl(),
    description = randomDescription(),
    size = "${String.format("%.02f", nextFloat() * 5f)} m",
    weight = "${String.format("%.02f", nextFloat() * 20f)} kg",
    atk = Random.nextInt(500),
    def = Random.nextInt(300)
  )

private val nameChoices = listOf(
  listOf("Red", "Blue", "Yellow", "Mega", "Tiny"),
  listOf("Juniper", "Freddy", "Jitter", "Ytong", "Hoge"),
  listOf("Ladybug", "Scarab", "Jollywalker", "Pincer", "Buggers")
)

private fun randomBugName(): String =
  nameChoices
    .map { choices -> choices.random() }
    .run { if (Random.nextBoolean()) dropLast(1) else this }
    .run { if (Random.nextBoolean()) drop(1) else this }
    .joinToString(" ")

private fun randomUrl(): String {
  // Photos by various artists on Unsplash:
  // - @alanemery
  // - @crypticsy
  // - @davidclode
  // - @simplysuzy
  // - @epan5
  val rnd = Random.nextInt(100)

  return when {
    rnd < 20 -> "https://i.imgur.com/qnta4FE.jpg"
    rnd < 40 -> "https://i.imgur.com/myDXq4b.jpg"
    rnd < 60 -> "https://i.imgur.com/q5ZE1KX.jpg"
    rnd < 80 -> "https://i.imgur.com/3kUs9z6.jpg"
    else -> "https://i.imgur.com/bdu26LQ.jpg"
  }
}

private val descriptionChoices = """
Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium,
totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae
dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit,
sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam
est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius
modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima
veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea
commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam
nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?
""".trim().replace("\n", " ").split(" ").filter { it.trim().isNotEmpty() }

private fun randomDescription(): String? =
  if (Random.nextInt(100) > 25) {
    // Construct a description from random words
    (0..30 + Random.nextInt(50)).joinToString(" ") { descriptionChoices.random() }
  } else {
    null
  }
