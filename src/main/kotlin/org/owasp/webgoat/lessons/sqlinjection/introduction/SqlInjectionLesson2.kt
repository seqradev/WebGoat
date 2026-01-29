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

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint2-1",
        "SqlStringInjectionHint2-2",
        "SqlStringInjectionHint2-3",
        "SqlStringInjectionHint2-4",
    ],
)
class SqlInjectionLesson2(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack2")
    @ResponseBody
    fun completed(
        @RequestParam query: String,
    ): AttackResult = injectableQuery(query)

    protected fun injectableQuery(query: String): AttackResult {
        try {
            dataSource.connection.use { connection ->
                connection
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                    .use { statement ->
                        statement.executeQuery(query).use { results ->
                            val output = StringBuilder()

                            results.first()

                            return if (results.getString("department") == "Marketing") {
                                output.append("<span class='feedback-positive'>$query</span>")
                                output.append(SqlInjectionLesson8.generateTable(results))
                                success(this).feedback("sql-injection.2.success").output(output.toString()).build()
                            } else {
                                failed(this).feedback("sql-injection.2.failed").output(output.toString()).build()
                            }
                        }
                    }
            }
        } catch (sqle: java.sql.SQLException) {
            return failed(this).feedback("sql-injection.2.failed").output(sqle.message).build()
        }
    }
}
