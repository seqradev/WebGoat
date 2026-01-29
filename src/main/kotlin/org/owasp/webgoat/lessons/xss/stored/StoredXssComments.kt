/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss.stored

import com.fasterxml.jackson.databind.ObjectMapper
import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.xss.Comment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class StoredXssComments : AssignmentEndpoint {
    @GetMapping(
        path = ["/CrossSiteScriptingStored/stored-xss"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.ALL_VALUE],
    )
    @ResponseBody
    fun retrieveComments(
        @CurrentUsername username: String?,
    ): Collection<Comment> = (comments + userComments[username].orEmpty()).asReversed()

    @PostMapping("/CrossSiteScriptingStored/stored-xss")
    @ResponseBody
    fun createNewComment(
        @RequestBody commentStr: String,
        @CurrentUsername username: String?,
    ): AttackResult {
        val comment =
            parseJson(commentStr).apply {
                dateTime = LocalDateTime.now().format(FMT)
                user = username
            }

        userComments.getOrPut(username) { mutableListOf() }.add(comment)

        return if (comment.text?.contains(PHONE_HOME_STRING) == true) {
            success(this).feedback("xss-stored-comment-success").build()
        } else {
            failed(this).feedback("xss-stored-comment-failure").build()
        }
    }

    private fun parseJson(comment: String): Comment =
        runCatching { ObjectMapper().readValue(comment, Comment::class.java) }
            .getOrElse { Comment() }

    companion object {
        private val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        private val userComments = mutableMapOf<String?, MutableList<Comment>>()
        private val comments = mutableListOf<Comment>()
        private const val PHONE_HOME_STRING = "<script>webgoat.customjs.phoneHome()</script>"

        init {
            comments.add(
                Comment(
                    "secUriTy",
                    LocalDateTime.now().format(FMT),
                    "<script>console.warn('unit test me')</script>Comment for Unit Testing",
                ),
            )
            comments.add(Comment("webgoat", LocalDateTime.now().format(FMT), "This comment is safe"))
            comments.add(Comment("guest", LocalDateTime.now().format(FMT), "This one is safe too."))
            comments.add(
                Comment(
                    "guest",
                    LocalDateTime.now().format(FMT),
                    "Can you post a comment, calling webgoat.customjs.phoneHome() ?",
                ),
            )
        }
    }
}
