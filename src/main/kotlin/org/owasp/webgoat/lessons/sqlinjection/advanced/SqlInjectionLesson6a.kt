/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.sqlinjection.introduction.SqlInjectionLesson5a
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.sql.ResultSet
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint-advanced-6a-1",
        "SqlStringInjectionHint-advanced-6a-2",
        "SqlStringInjectionHint-advanced-6a-3",
        "SqlStringInjectionHint-advanced-6a-4",
        "SqlStringInjectionHint-advanced-6a-5",
    ],
)
class SqlInjectionLesson6a(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjectionAdvanced/attack6a")
    @ResponseBody
    fun completed(
        @RequestParam(value = "userid_6a") userId: String,
    ): AttackResult = injectableQuery(userId)
    // The answer: Smith' union select userid,user_name, password,cookie,cookie, cookie,userid from
    // user_system_data --

    fun injectableQuery(accountName: String): AttackResult {
        var query = ""
        try {
            dataSource.connection.use { connection ->
                val usedUnion = unionQueryChecker(accountName)
                query = "SELECT * FROM user_data WHERE last_name = '$accountName'"

                return executeSqlInjection(connection, query, usedUnion)
            }
        } catch (e: Exception) {
            return failed(this)
                .output("${this.javaClass.name} : ${e.message}$YOUR_QUERY_WAS$query")
                .build()
        }
    }

    private fun unionQueryChecker(accountName: String): Boolean =
        accountName.matches(Regex("(?i)(^[^-/*;)]*)(\\s*)UNION(.*$)"))

    private fun executeSqlInjection(
        connection: java.sql.Connection,
        query: String,
        usedUnion: Boolean,
    ): AttackResult {
        try {
            connection
                .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .use { statement ->
                    val results = statement.executeQuery(query)

                    if (!(results != null && results.first())) {
                        return failed(this)
                            .feedback("sql-injection.advanced.6a.no.results")
                            .output("$YOUR_QUERY_WAS$query")
                            .build()
                    }

                    val resultsMetaData = results.metaData
                    val output = StringBuilder()
                    val appendingWhenSucceeded = appendSucceededMessage(usedUnion)

                    output.append(SqlInjectionLesson5a.writeTable(results, resultsMetaData))
                    results.last()

                    return verifySqlInjection(output, appendingWhenSucceeded, query)
                }
        } catch (sqle: SQLException) {
            return failed(this).output("${sqle.message}$YOUR_QUERY_WAS$query").build()
        }
    }

    private fun appendSucceededMessage(isUsedUnion: Boolean): String {
        var appendingWhenSucceeded = "Well done! Can you also figure out a solution, by "

        appendingWhenSucceeded += if (isUsedUnion) "appending a new SQL Statement?" else "using a UNION?"

        return appendingWhenSucceeded
    }

    private fun verifySqlInjection(
        output: StringBuilder,
        appendingWhenSucceeded: String,
        query: String,
    ): AttackResult {
        if (!(output.toString().contains("dave") && output.toString().contains("passW0rD"))) {
            return failed(this).output("$output$YOUR_QUERY_WAS$query").build()
        }

        output.append(appendingWhenSucceeded)
        return success(this)
            .feedback("sql-injection.advanced.6a.success")
            .feedbackArgs(output.toString())
            .output(" Your query was: $query")
            .build()
    }

    companion object {
        private const val YOUR_QUERY_WAS = "<br> Your query was: "
    }
}
