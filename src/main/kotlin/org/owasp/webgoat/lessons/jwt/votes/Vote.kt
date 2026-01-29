/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt.votes

import com.fasterxml.jackson.annotation.JsonView
import kotlin.math.roundToLong

class Vote(
    @field:JsonView(Views.GuestView::class)
    val title: String,
    @field:JsonView(Views.GuestView::class)
    val information: String,
    @field:JsonView(Views.GuestView::class)
    val imageSmall: String,
    @field:JsonView(Views.GuestView::class)
    val imageBig: String,
    @field:JsonView(Views.UserView::class)
    var numberOfVotes: Int,
    totalVotes: Int,
) {
    @field:JsonView(Views.UserView::class)
    var votingAllowed: Boolean = true
        private set

    @field:JsonView(Views.UserView::class)
    var average: Long = calculateStars(totalVotes)
        private set

    fun incrementNumberOfVotes(totalVotes: Int) {
        numberOfVotes++
        average = calculateStars(totalVotes)
    }

    fun reset() {
        numberOfVotes = 1
        average = 1
    }

    private fun calculateStars(totalVotes: Int): Long =
        (numberOfVotes.toDouble() / totalVotes.toDouble() * 4).roundToLong()
}
