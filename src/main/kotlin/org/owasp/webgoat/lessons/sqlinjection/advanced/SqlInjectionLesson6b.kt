/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
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
import java.sql.ResultSet
import java.sql.SQLException

@RestController
class SqlInjectionLesson6b(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjectionAdvanced/attack6b")
    @ResponseBody
    fun completed(
        @RequestParam userid_6b: String,
    ): AttackResult =
        if (userid_6b == getPassword()) {
            success(this).build()
        } else {
            failed(this).build()
        }

    protected fun getPassword(): String {
        var password = "dave"
        try {
            dataSource.connection.use { connection ->
                val query = "SELECT password FROM user_system_data WHERE user_name = 'dave'"
                try {
                    connection
                        .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                        .use { statement ->
                            statement.executeQuery(query).use { results ->
                                if (results.first()) {
                                    password = results.getString("password")
                                }
                            }
                        }
                } catch (sqle: SQLException) {
                    sqle.printStackTrace()
                    // do nothing
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // do nothing
        }
        return password
    }
}
