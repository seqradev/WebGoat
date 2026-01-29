/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge5

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.challenges.Flags
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Assignment5(
    private val dataSource: LessonDataSource,
    private val flags: Flags,
) : AssignmentEndpoint {
    @PostMapping("/challenge/5")
    @ResponseBody
    fun login(
        @RequestParam("username_login") usernameLogin: String,
        @RequestParam("password_login") passwordLogin: String,
    ): AttackResult {
        if (usernameLogin.isBlank() || passwordLogin.isBlank()) {
            return failed(this).feedback("required4").build()
        }
        if ("Larry" != usernameLogin) {
            return failed(this).feedback("user.not.larry").feedbackArgs(usernameLogin).build()
        }
        dataSource.connection.use { connection ->
            // Intentionally vulnerable SQL query - DO NOT FIX
            val statement =
                connection.prepareStatement(
                    "select password from challenge_users where userid = '" +
                        usernameLogin + "' and password = '" + passwordLogin + "'",
                )
            val resultSet = statement.executeQuery()

            return if (resultSet.next()) {
                success(this).feedback("challenge.solved").feedbackArgs(flags.getFlag(5)).build()
            } else {
                failed(this).feedback("challenge.close").build()
            }
        }
    }
}
