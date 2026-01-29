/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@AssignmentHints("csrf-review-hint1", "csrf-review-hint2", "csrf-review-hint3")
class ForgedReviews : AssignmentEndpoint {
    @GetMapping(
        path = ["/csrf/review"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.ALL_VALUE],
    )
    @ResponseBody
    fun retrieveReviews(
        @CurrentUsername username: String?,
    ): Collection<Review> =
        buildList {
            userReviews[username]?.let { addAll(it) }
            addAll(REVIEWS)
        }

    @PostMapping("/csrf/review")
    @ResponseBody
    fun createNewReview(
        reviewText: String?,
        stars: Int?,
        validateReq: String?,
        request: HttpServletRequest,
        @CurrentUsername username: String?,
    ): AttackResult {
        val host = request.getHeader("host") ?: "NULL"
        val referer = request.getHeader("referer") ?: "NULL"
        val refererArr = referer.split("/")

        val review =
            Review().apply {
                text = reviewText
                dateTime = LocalDateTime.now().format(fmt)
                user = username
                this.stars = stars
            }
        val reviews = userReviews.getOrDefault(username, mutableListOf())
        reviews.add(review)
        userReviews[username] = reviews
        // short-circuit
        if (validateReq == null || validateReq != WEAK_ANTI_CSRF) {
            return failed(this).feedback("csrf-you-forgot-something").build()
        }
        // we have the spoofed files
        return if (referer != "NULL" && refererArr[2] == host) {
            failed(this).feedback("csrf-same-host").build()
        } else {
            success(this)
                .feedback("csrf-review.success")
                .build()
        }
    }

    companion object {
        private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")

        private val userReviews = mutableMapOf<String?, MutableList<Review>>()
        private val REVIEWS = mutableListOf<Review>()
        private const val WEAK_ANTI_CSRF = "2aa14227b9a13d0bede0388a7fba9aa9"

        init {
            REVIEWS.add(
                Review("secUriTy", LocalDateTime.now().format(fmt), "This is like swiss cheese", 0),
            )
            REVIEWS.add(Review("webgoat", LocalDateTime.now().format(fmt), "It works, sorta", 2))
            REVIEWS.add(Review("guest", LocalDateTime.now().format(fmt), "Best, App, Ever", 5))
            REVIEWS.add(
                Review(
                    "guest",
                    LocalDateTime.now().format(fmt),
                    "This app is so insecure, I didn't even post this review, can you pull that off too?",
                    1,
                ),
            )
        }
    }
}
