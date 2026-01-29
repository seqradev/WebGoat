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
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint.9.1",
        "SqlStringInjectionHint.9.2",
        "SqlStringInjectionHint.9.3",
        "SqlStringInjectionHint.9.4",
        "SqlStringInjectionHint.9.5",
    ],
)
class SqlInjectionLesson9(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjection/attack9")
    @ResponseBody
    fun completed(
        @RequestParam name: String,
        @RequestParam auth_tan: String,
    ): AttackResult = injectableQueryIntegrity(name, auth_tan)

    protected fun injectableQueryIntegrity(
        name: String,
        authTan: String,
    ): AttackResult {
        val output = StringBuilder()
        val queryInjection = "SELECT * FROM employees WHERE last_name = '$name' AND auth_tan = '$authTan'"
        try {
            dataSource.connection.use { connection ->
                // V2019_09_26_7__employees.sql
                val oldMaxSalary = getMaxSalary(connection)
                val oldSumSalariesOfOtherEmployees = getSumSalariesOfOtherEmployees(connection)
                // begin transaction
                connection.autoCommit = false
                // do injectable query
                connection
                    .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
                    .use { statement ->
                        SqlInjectionLesson8.log(connection, queryInjection)
                        statement.execute(queryInjection)
                    }
                // check new sum of salaries other employees and new salaries of John
                val newJohnSalary = getJohnSalary(connection)
                val newSumSalariesOfOtherEmployees = getSumSalariesOfOtherEmployees(connection)
                if (newJohnSalary > oldMaxSalary &&
                    newSumSalariesOfOtherEmployees == oldSumSalariesOfOtherEmployees
                ) {
                    // success commit
                    connection.commit() // need execute not executeQuery
                    connection.autoCommit = true
                    output.append(
                        SqlInjectionLesson8.generateTable(getEmployeesDataOrderBySalaryDesc(connection)),
                    )
                    return success(this).feedback("sql-injection.9.success").output(output.toString()).build()
                }
                // failed rollback
                connection.rollback()
                return failed(this)
                    .feedback("sql-injection.9.one")
                    .output(
                        SqlInjectionLesson8.generateTable(getEmployeesDataOrderBySalaryDesc(connection)),
                    ).build()
            }
        } catch (e: SQLException) {
            return failed(this)
                .output("<br><span class='feedback-negative'>${e.message}</span>")
                .build()
        }
    }

    @Throws(SQLException::class)
    private fun getSqlInt(
        connection: Connection,
        query: String,
    ): Int =
        connection
            .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
            .use { statement ->
                statement.executeQuery(query).use { results ->
                    results.first()
                    results.getInt(1)
                }
            }

    @Throws(SQLException::class)
    private fun getMaxSalary(connection: Connection): Int {
        val query = "SELECT max(salary) FROM employees"
        return getSqlInt(connection, query)
    }

    @Throws(SQLException::class)
    private fun getSumSalariesOfOtherEmployees(connection: Connection): Int {
        val query = "SELECT sum(salary) FROM employees WHERE auth_tan != '3SL99A'"
        return getSqlInt(connection, query)
    }

    @Throws(SQLException::class)
    private fun getJohnSalary(connection: Connection): Int {
        val query = "SELECT salary FROM employees WHERE auth_tan = '3SL99A'"
        return getSqlInt(connection, query)
    }

    @Throws(SQLException::class)
    private fun getEmployeesDataOrderBySalaryDesc(connection: Connection): ResultSet {
        val query = "SELECT * FROM employees ORDER BY salary DESC"
        val statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
        return statement.executeQuery(query)
    }
}
