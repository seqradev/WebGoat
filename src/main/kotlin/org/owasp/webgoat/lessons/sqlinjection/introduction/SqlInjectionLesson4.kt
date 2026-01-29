/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
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
@AssignmentHints(
    value = ["SqlStringInjectionHint4-1", "SqlStringInjectionHint4-2", "SqlStringInjectionHint4-3"],
)
class SqlInjectionLesson4(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack4")
    @ResponseBody
    fun completed(
        @RequestParam query: String,
    ): AttackResult = injectableQuery(query)

    protected fun injectableQuery(query: String): AttackResult {
        try {
            dataSource.connection.use { connection ->
                try {
                    connection
                        .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                        .use { statement ->
                            statement.executeUpdate(query)
                            connection.commit()
                            val results = statement.executeQuery("SELECT phone from employees;")
                            val output = StringBuilder()
                            // user completes lesson if column phone exists
                            return if (results.first()) {
                                output.append("<span class='feedback-positive'>$query</span>")
                                success(this).output(output.toString()).build()
                            } else {
                                failed(this).output(output.toString()).build()
                            }
                        }
                } catch (sqle: SQLException) {
                    return failed(this).output(sqle.message).build()
                }
            }
        } catch (e: Exception) {
            return failed(this).output("${this.javaClass.name} : ${e.message}").build()
        }
    }
}
