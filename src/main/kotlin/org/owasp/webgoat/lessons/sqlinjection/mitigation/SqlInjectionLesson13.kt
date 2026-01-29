/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.owasp.webgoat.container.LessonDataSource
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.sql.SQLException

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint-mitigation-13-1",
        "SqlStringInjectionHint-mitigation-13-2",
        "SqlStringInjectionHint-mitigation-13-3",
        "SqlStringInjectionHint-mitigation-13-4",
    ],
)
class SqlInjectionLesson13(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("/SqlInjectionMitigations/attack12a")
    @ResponseBody
    fun completed(
        @RequestParam ip: String,
    ): AttackResult {
        try {
            dataSource.connection.use { connection ->
                connection
                    .prepareStatement("select ip from servers where ip = ? and hostname = ?")
                    .use { preparedStatement ->
                        preparedStatement.setString(1, ip)
                        preparedStatement.setString(2, "webgoat-prd")
                        val resultSet = preparedStatement.executeQuery()
                        return if (resultSet.next()) {
                            success(this).build()
                        } else {
                            failed(this).build()
                        }
                    }
            }
        } catch (e: SQLException) {
            log.error("Failed", e)
            return failed(this).build()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SqlInjectionLesson13::class.java)
    }
}
