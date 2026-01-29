/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import org.apache.commons.lang3.RandomStringUtils
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.lessons.Initializable
import org.owasp.webgoat.container.users.WebGoatUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@RestController
@AssignmentHints(
    "xxe.blind.hints.1",
    "xxe.blind.hints.2",
    "xxe.blind.hints.3",
    "xxe.blind.hints.4",
    "xxe.blind.hints.5",
)
class BlindSendFileAssignment(
    @Value("\${webgoat.user.directory}") private val webGoatHomeDirectory: String,
    private val comments: CommentsCache,
) : AssignmentEndpoint,
    Initializable {
    private val log = LoggerFactory.getLogger(BlindSendFileAssignment::class.java)
    private val userToFileContents = mutableMapOf<WebGoatUser, String>()

    private fun createSecretFileWithRandomContents(user: WebGoatUser) {
        val fileContents = "WebGoat 8.0 rocks... (${RandomStringUtils.randomAlphabetic(10)})"
        userToFileContents[user] = fileContents
        val targetDirectory = File(webGoatHomeDirectory, "/XXE/${user.username}")
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs()
        }
        try {
            Files.writeString(File(targetDirectory, "secret.txt").toPath(), fileContents, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            log.error("Unable to write 'secret.txt' to '{}", targetDirectory)
        }
    }

    @PostMapping(
        path = ["xxe/blind"],
        consumes = [org.springframework.http.MediaType.ALL_VALUE],
        produces = [org.springframework.http.MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun addComment(
        @RequestBody commentStr: String,
        @AuthenticationPrincipal user: WebGoatUser,
    ): AttackResult {
        val fileContentsForUser = userToFileContents.getOrDefault(user, "")

        // Solution is posted by the user as a separate comment
        if (commentStr.contains(fileContentsForUser)) {
            return success(this).build()
        }

        try {
            val comment = comments.parseXml(commentStr, false)
            if (fileContentsForUser.contains(comment.text ?: "")) {
                comment.text = "Nice try, you need to send the file to WebWolf"
            }
            comments.addComment(comment, user, false)
        } catch (e: Exception) {
            return failed(this).output(e.toString()).build()
        }
        return failed(this).build()
    }

    override fun initialize(user: WebGoatUser) {
        comments.reset(user)
        userToFileContents.remove(user)
        createSecretFileWithRandomContents(user)
    }
}
