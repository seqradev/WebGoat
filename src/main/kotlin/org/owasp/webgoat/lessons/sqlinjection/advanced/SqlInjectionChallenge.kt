/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlInjectionChallenge1",
        "SqlInjectionChallenge2",
        "SqlInjectionChallenge3",
        "SqlInjectionChallenge4",
        "SqlInjectionChallenge5",
        "SqlInjectionChallenge6",
        "SqlInjectionChallenge7",
    ],
)
class SqlInjectionChallenge(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PutMapping("/SqlInjectionAdvanced/register")
    // assignment path is bounded to class so we use different http method :-)
    @ResponseBody
    fun registerNewUser(
        @RequestParam("username_reg") username: String,
        @RequestParam("email_reg") email: String,
        @RequestParam("password_reg") password: String,
    ): AttackResult {
        checkArguments(username, email, password)?.let { return it }

        return try {
            dataSource.connection.use { connection ->
                val checkUserQuery =
                    "select userid from sql_challenge_users where userid = '$username'"
                connection.createStatement().use { statement ->
                    statement.executeQuery(checkUserQuery).use { resultSet ->
                        if (resultSet.next()) {
                            failed(this).feedback("user.exists").feedbackArgs(username).build()
                        } else {
                            connection
                                .prepareStatement("INSERT INTO sql_challenge_users VALUES (?, ?, ?)")
                                .use { preparedStatement ->
                                    preparedStatement.setString(1, username)
                                    preparedStatement.setString(2, email)
                                    preparedStatement.setString(3, password)
                                    preparedStatement.execute()
                                    informationMessage(this).feedback("user.created").feedbackArgs(username).build()
                                }
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            failed(this).output("Something went wrong").build()
        }
    }

    private fun checkArguments(
        username: String,
        email: String,
        password: String,
    ): AttackResult? {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return failed(this).feedback("input.invalid").build()
        }
        if (username.length > 250 || email.length > 30 || password.length > 30) {
            return failed(this).feedback("input.invalid").build()
        }
        return null
    }
}
