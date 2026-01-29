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
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Calendar

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint.8.1",
        "SqlStringInjectionHint.8.2",
        "SqlStringInjectionHint.8.3",
        "SqlStringInjectionHint.8.4",
        "SqlStringInjectionHint.8.5",
    ],
)
class SqlInjectionLesson8(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack8")
    @ResponseBody
    fun completed(
        @RequestParam name: String,
        @RequestParam auth_tan: String,
    ): AttackResult = injectableQueryConfidentiality(name, auth_tan)

    protected fun injectableQueryConfidentiality(
        name: String,
        authTan: String,
    ): AttackResult {
        val output = StringBuilder()
        val query = "SELECT * FROM employees WHERE last_name = '$name' AND auth_tan = '$authTan'"

        try {
            dataSource.connection.use { connection ->
                try {
                    connection
                        .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                        .use { statement ->
                            log(connection, query)
                            val results = statement.executeQuery(query)

                            if (results.statement != null) {
                                if (results.first()) {
                                    output.append(generateTable(results))
                                    results.last()

                                    return if (results.row > 1) {
                                        // more than one record, the user succeeded
                                        success(this)
                                            .feedback("sql-injection.8.success")
                                            .output(output.toString())
                                            .build()
                                    } else {
                                        // only one record
                                        failed(this).feedback("sql-injection.8.one").output(output.toString()).build()
                                    }
                                } else {
                                    // no results
                                    return failed(this).feedback("sql-injection.8.no.results").build()
                                }
                            } else {
                                return failed(this).build()
                            }
                        }
                } catch (e: SQLException) {
                    return failed(this)
                        .output("<br><span class='feedback-negative'>${e.message}</span>")
                        .build()
                }
            }
        } catch (e: Exception) {
            return failed(this)
                .output("<br><span class='feedback-negative'>${e.message}</span>")
                .build()
        }
    }

    companion object {
        @JvmStatic
        @Throws(SQLException::class)
        fun generateTable(results: ResultSet): String {
            val resultsMetaData = results.metaData
            val numColumns = resultsMetaData.columnCount
            results.beforeFirst()
            val table = StringBuilder()
            table.append("<table>")

            if (results.next()) {
                table.append("<tr>")
                for (i in 1..numColumns) {
                    table.append("<th>${resultsMetaData.getColumnName(i)}</th>")
                }
                table.append("</tr>")

                results.beforeFirst()
                while (results.next()) {
                    table.append("<tr>")
                    for (i in 1..numColumns) {
                        table.append("<td>${results.getString(i)}</td>")
                    }
                    table.append("</tr>")
                }
            } else {
                table.append("Query Successful; however no data was returned from this query.")
            }

            table.append("</table>")
            return table.toString()
        }

        @JvmStatic
        fun log(
            connection: Connection,
            action: String,
        ) {
            val actionEscaped = action.replace('\'', '"')
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val time = sdf.format(cal.time)

            val logQuery = "INSERT INTO access_log (time, action) VALUES ('$time', '$actionEscaped')"

            try {
                connection
                    .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
                    .use { statement ->
                        statement.executeUpdate(logQuery)
                    }
            } catch (e: SQLException) {
                System.err.println(e.message)
            }
        }
    }
}
