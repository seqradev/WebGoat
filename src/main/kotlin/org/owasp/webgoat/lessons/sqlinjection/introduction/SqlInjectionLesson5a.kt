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
import java.sql.ResultSetMetaData
import java.sql.SQLException

@RestController
@AssignmentHints(value = ["SqlStringInjectionHint5a1"])
class SqlInjectionLesson5a(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/assignment5a")
    @ResponseBody
    fun completed(
        @RequestParam account: String,
        @RequestParam operator: String,
        @RequestParam injection: String,
    ): AttackResult = injectableQuery("$account $operator $injection")

    protected fun injectableQuery(accountName: String): AttackResult {
        var query = ""
        try {
            dataSource.connection.use { connection ->
                query = "SELECT * FROM user_data WHERE first_name = 'John' and last_name = '$accountName'"
                connection
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                    .use { statement ->
                        val results = statement.executeQuery(query)

                        if (results != null && results.first()) {
                            val resultsMetaData = results.metaData
                            val output = StringBuilder()

                            output.append(writeTable(results, resultsMetaData))
                            results.last()

                            // If they get back more than one user they succeeded
                            return if (results.row >= 6) {
                                success(this)
                                    .feedback("sql-injection.5a.success")
                                    .output("Your query was: $query$EXPLANATION")
                                    .feedbackArgs(output.toString())
                                    .build()
                            } else {
                                failed(this).output("$output<br> Your query was: $query").build()
                            }
                        } else {
                            return failed(this)
                                .feedback("sql-injection.5a.no.results")
                                .output("Your query was: $query")
                                .build()
                        }
                    }
            }
        } catch (sqle: SQLException) {
            return failed(this).output("${sqle.message}<br> Your query was: $query").build()
        } catch (e: Exception) {
            return failed(this)
                .output("${this.javaClass.name} : ${e.message}<br> Your query was: $query")
                .build()
        }
    }

    companion object {
        private const val EXPLANATION =
            "<br> Explanation: This injection works, because <span style=\"font-style: italic\">or '1' =" +
                " '1'</span> always evaluates to true (The string ending literal for '1 is closed by" +
                " the query itself, so you should not inject it). So the injected query basically looks" +
                " like this: <span style=\"font-style: italic\">SELECT * FROM user_data WHERE" +
                " (first_name = 'John' and last_name = '') or (TRUE)</span>, which will always evaluate" +
                " to true, no matter what came before it."

        @JvmStatic
        @Throws(SQLException::class)
        fun writeTable(
            results: ResultSet,
            resultsMetaData: ResultSetMetaData,
        ): String {
            val numColumns = resultsMetaData.columnCount
            results.beforeFirst()
            val t = StringBuilder()
            t.append("<p>")

            if (results.next()) {
                for (i in 1..(numColumns)) {
                    t.append(resultsMetaData.getColumnName(i))
                    t.append(", ")
                }

                t.append("<br />")
                results.beforeFirst()

                while (results.next()) {
                    for (i in 1..(numColumns)) {
                        t.append(results.getString(i))
                        t.append(", ")
                    }

                    t.append("<br />")
                }
            } else {
                t.append("Query Successful; however no data was returned from this query.")
            }

            t.append("</p>")
            return t.toString()
        }
    }
}
