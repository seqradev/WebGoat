/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
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
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint.10.1",
        "SqlStringInjectionHint.10.2",
        "SqlStringInjectionHint.10.3",
        "SqlStringInjectionHint.10.4",
        "SqlStringInjectionHint.10.5",
        "SqlStringInjectionHint.10.6",
    ],
)
class SqlInjectionLesson10(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack10")
    @ResponseBody
    fun completed(
        @RequestParam action_string: String,
    ): AttackResult = injectableQueryAvailability(action_string)

    protected fun injectableQueryAvailability(action: String): AttackResult {
        val output = StringBuilder()
        val query = "SELECT * FROM access_log WHERE action LIKE '%$action%'"

        try {
            dataSource.connection.use { connection ->
                try {
                    connection
                        .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                        .use { statement ->
                            val results = statement.executeQuery(query)

                            if (results.statement != null) {
                                results.first()
                                output.append(SqlInjectionLesson8.generateTable(results))
                                return failed(this)
                                    .feedback("sql-injection.10.entries")
                                    .output(output.toString())
                                    .build()
                            } else {
                                return if (tableExists(connection)) {
                                    failed(this)
                                        .feedback("sql-injection.10.entries")
                                        .output(output.toString())
                                        .build()
                                } else {
                                    success(this).feedback("sql-injection.10.success").build()
                                }
                            }
                        }
                } catch (e: SQLException) {
                    return if (tableExists(connection)) {
                        failed(this)
                            .output(
                                "<span class='feedback-negative'>${e.message}</span><br>$output",
                            ).build()
                    } else {
                        success(this).feedback("sql-injection.10.success").build()
                    }
                }
            }
        } catch (e: Exception) {
            return failed(this)
                .output("<span class='feedback-negative'>${e.message}</span>")
                .build()
        }
    }

    private fun tableExists(connection: Connection): Boolean =
        try {
            connection
                .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .use { stmt ->
                    stmt.executeQuery("SELECT * FROM access_log").use { results ->
                        results.metaData.columnCount > 0
                    }
                }
        } catch (e: SQLException) {
            e.message?.contains("object not found: ACCESS_LOG") != true
        }
}
