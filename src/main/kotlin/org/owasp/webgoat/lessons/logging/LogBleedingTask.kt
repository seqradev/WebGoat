/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.logging

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID

@RestController
class LogBleedingTask : AssignmentEndpoint {
    private val password: String = UUID.randomUUID().toString()

    init {
        log.info(
            "Password for admin: {}",
            Base64.getEncoder().encodeToString(password.toByteArray(StandardCharsets.UTF_8)),
        )
    }

    @PostMapping("/LogSpoofing/log-bleeding")
    @ResponseBody
    fun completed(
        @RequestParam username: String?,
        @RequestParam password: String?,
    ): AttackResult {
        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            return failed(this).output("Please provide username (Admin) and password").build()
        }

        return if (username == "Admin" && password == this.password) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LogBleedingTask::class.java)
    }
}
