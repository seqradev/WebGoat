/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
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
@AssignmentHints(value = ["SqlStringInjectionHint3-1", "SqlStringInjectionHint3-2"])
class SqlInjectionLesson3(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack3")
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
                            val checkStatement =
                                connection.createStatement(
                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                    ResultSet.CONCUR_READ_ONLY,
                                )
                            statement.executeUpdate(query)
                            val results =
                                checkStatement.executeQuery("SELECT * FROM employees WHERE last_name='Barnett';")
                            val output = StringBuilder()
                            // user completes lesson if the department of Tobi Barnett now is 'Sales'
                            results.first()
                            return if (results.getString("department") == "Sales") {
                                output.append("<span class='feedback-positive'>$query</span>")
                                output.append(SqlInjectionLesson8.generateTable(results))
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
