/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
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
    value = [
        "SqlStringInjectionHint5b1",
        "SqlStringInjectionHint5b2",
        "SqlStringInjectionHint5b3",
        "SqlStringInjectionHint5b4",
    ],
)
class SqlInjectionLesson5b(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/assignment5b")
    @ResponseBody
    fun completed(
        @RequestParam userid: String,
        @RequestParam login_count: String,
    ): AttackResult = injectableQuery(login_count, userid)

    protected fun injectableQuery(
        loginCount: String,
        accountName: String,
    ): AttackResult {
        val queryString = "SELECT * From user_data WHERE Login_Count = ? and userid= $accountName"
        try {
            dataSource.connection.use { connection ->
                connection
                    .prepareStatement(
                        queryString,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                    ).use { query ->
                        val count: Int
                        try {
                            count = loginCount.toInt()
                        } catch (e: Exception) {
                            return failed(this)
                                .output(
                                    "Could not parse: $loginCount to a number<br> Your query was: ${
                                        queryString.replace(
                                            "?",
                                            loginCount,
                                        )
                                    }",
                                ).build()
                        }

                        query.setInt(1, count)
                        try {
                            query.executeQuery().use { results ->
                                if (results.first()) {
                                    val resultsMetaData = results.metaData
                                    val output = StringBuilder()

                                    output.append(SqlInjectionLesson5a.writeTable(results, resultsMetaData))
                                    results.last()

                                    // If they get back more than one user they succeeded
                                    return if (results.row >= 6) {
                                        success(this)
                                            .feedback("sql-injection.5b.success")
                                            .output("Your query was: ${queryString.replace("?", loginCount)}")
                                            .feedbackArgs(output.toString())
                                            .build()
                                    } else {
                                        failed(this)
                                            .output(
                                                "$output<br> Your query was: ${queryString.replace("?", loginCount)}",
                                            ).build()
                                    }
                                } else {
                                    return failed(this)
                                        .feedback("sql-injection.5b.no.results")
                                        .output("Your query was: ${queryString.replace("?", loginCount)}")
                                        .build()
                                }
                            }
                        } catch (sqle: SQLException) {
                            return failed(this)
                                .output("${sqle.message}<br> Your query was: ${queryString.replace("?", loginCount)}")
                                .build()
                        }
                    }
            }
        } catch (e: Exception) {
            return failed(this)
                .output(
                    "${this.javaClass.name} : ${e.message}<br> Your query was: ${queryString.replace("?", loginCount)}",
                ).build()
        }
    }
}
