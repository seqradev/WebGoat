/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.logging

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LogSpoofingTask : AssignmentEndpoint {
    @PostMapping("/LogSpoofing/log-spoofing")
    @ResponseBody
    fun completed(
        @RequestParam username: String?,
        @RequestParam password: String?,
    ): AttackResult {
        if (username.isNullOrEmpty()) {
            return failed(this).output(username).build()
        }
        val processedUsername = username.replace("\n", "<br/>")
        if (processedUsername.contains("<p>") || processedUsername.contains("<div>")) {
            return failed(this).output("Try to think of something simple ").build()
        }
        return if (processedUsername.indexOf("<br/>") < processedUsername.indexOf("admin")) {
            success(this).output(processedUsername).build()
        } else {
            failed(this).output(processedUsername).build()
        }
    }
}
