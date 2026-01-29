/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SqlInjectionChallengeLogin(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjectionAdvanced/login")
    @ResponseBody
    fun login(
        @RequestParam("username_login") username: String,
        @RequestParam("password_login") password: String,
    ): AttackResult {
        dataSource.connection.use { connection ->
            connection
                .prepareStatement(
                    "select password from sql_challenge_users where userid = ? and password = ?",
                ).use { statement ->
                    statement.setString(1, username)
                    statement.setString(2, password)
                    statement.executeQuery().use { resultSet ->
                        return if (resultSet.next()) {
                            if ("tom" == username) {
                                success(this).build()
                            } else {
                                failed(this).feedback("ResultsButNotTom").build()
                            }
                        } else {
                            failed(this).feedback("NoResultsMatched").build()
                        }
                    }
                }
        }
    }
}
