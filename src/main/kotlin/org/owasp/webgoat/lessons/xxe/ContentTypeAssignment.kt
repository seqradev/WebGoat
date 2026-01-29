/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.exec.OS
import org.apache.commons.lang3.exception.ExceptionUtils
import org.owasp.webgoat.container.CurrentUser
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("xxe.hints.content.type.xxe.1", "xxe.hints.content.type.xxe.2")
class ContentTypeAssignment(
    private val comments: CommentsCache,
) : AssignmentEndpoint {
    companion object {
        private val DEFAULT_LINUX_DIRECTORIES = arrayOf("usr", "etc", "var")
        private val DEFAULT_WINDOWS_DIRECTORIES =
            arrayOf("Windows", "Program Files (x86)", "Program Files", "pagefile.sys")
    }

    @PostMapping(path = ["xxe/content-type"])
    @ResponseBody
    fun createNewUser(
        @RequestBody commentStr: String,
        @RequestHeader("Content-Type") contentType: String?,
        @CurrentUser user: WebGoatUser,
    ): AttackResult {
        var attackResult = failed(this).build()

        if (MediaType.APPLICATION_JSON_VALUE == contentType) {
            parseJson(commentStr)?.let { comments.addComment(it, user, true) }
            attackResult = failed(this).feedback("xxe.content.type.feedback.json").build()
        }

        if (contentType != null && contentType.contains(MediaType.APPLICATION_XML_VALUE)) {
            try {
                val comment = comments.parseXml(commentStr, false)
                comments.addComment(comment, user, false)
                if (checkSolution(comment)) {
                    attackResult = success(this).build()
                }
            } catch (e: Exception) {
                val error = ExceptionUtils.getStackTrace(e)
                attackResult = failed(this).feedback("xxe.content.type.feedback.xml").output(error).build()
            }
        }

        return attackResult
    }

    protected fun parseJson(comment: String): Comment? =
        runCatching { ObjectMapper().readValue(comment, Comment::class.java) }.getOrNull()

    private fun checkSolution(comment: Comment): Boolean {
        val directoriesToCheck =
            if (OS.isFamilyMac() || OS.isFamilyUnix()) {
                DEFAULT_LINUX_DIRECTORIES
            } else {
                DEFAULT_WINDOWS_DIRECTORIES
            }
        return directoriesToCheck.any { directory ->
            comment.text?.contains(directory) == true
        }
    }
}
