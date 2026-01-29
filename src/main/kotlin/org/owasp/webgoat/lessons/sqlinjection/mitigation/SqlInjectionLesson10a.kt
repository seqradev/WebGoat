/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    value = ["SqlStringInjectionHint-mitigation-10a-1", "SqlStringInjectionHint-mitigation-10a-2"],
)
class SqlInjectionLesson10a : AssignmentEndpoint {
    @PostMapping("/SqlInjectionMitigations/attack10a")
    @ResponseBody
    fun completed(
        @RequestParam field1: String,
        @RequestParam field2: String,
        @RequestParam field3: String,
        @RequestParam field4: String,
        @RequestParam field5: String,
        @RequestParam field6: String,
        @RequestParam field7: String,
    ): AttackResult {
        val userInput = arrayOf(field1, field2, field3, field4, field5, field6, field7)
        val allMatch =
            userInput.zip(RESULTS).all { (input, result) ->
                input.lowercase().contains(result.lowercase())
            }
        return if (allMatch) {
            success(this).build()
        } else {
            failed(this).build()
        }
    }

    companion object {
        private val RESULTS =
            arrayOf(
                "getConnection",
                "PreparedStatement",
                "prepareStatement",
                "?",
                "?",
                "setString",
                "setString",
            )
    }
}
