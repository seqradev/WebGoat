/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.introduction

import jakarta.annotation.PostConstruct
import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.sql.ResultSet
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint5-1",
        "SqlStringInjectionHint5-2",
        "SqlStringInjectionHint5-3",
        "SqlStringInjectionHint5-4",
    ],
)
class SqlInjectionLesson5(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostConstruct
    fun createUser() {
        // HSQLDB does not support CREATE USER with IF NOT EXISTS so we need to do it in code (using
        // DROP first will throw error if user does not exists)
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement("CREATE USER unauthorized_user PASSWORD test").use { statement ->
                    statement.execute()
                }
            }
        } catch (e: Exception) {
            // user already exists continue
        }
    }

    @PostMapping("/SqlInjection/attack5")
    @ResponseBody
    fun completed(query: String?): AttackResult {
        createUser()
        return injectableQuery(query)
    }

    protected fun injectableQuery(query: String?): AttackResult {
        try {
            dataSource.connection.use { connection ->
                connection
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
                    .use { statement ->
                        statement.executeQuery(query)
                        return if (checkSolution(connection)) {
                            success(this).build()
                        } else {
                            failed(this).output("Your query was: $query").build()
                        }
                    }
            }
        } catch (e: Exception) {
            return failed(this)
                .output("${this.javaClass.name} : ${e.message}<br> Your query was: $query")
                .build()
        }
    }

    private fun checkSolution(connection: java.sql.Connection): Boolean {
        try {
            connection
                .prepareStatement(
                    "SELECT * FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE TABLE_NAME = ? AND GRANTEE = ?",
                ).use { stmt ->
                    stmt.setString(1, "GRANT_RIGHTS")
                    stmt.setString(2, "UNAUTHORIZED_USER")
                    stmt.executeQuery().use { resultSet ->
                        return resultSet.next()
                    }
                }
        } catch (throwables: SQLException) {
            return false
        }
    }
}
