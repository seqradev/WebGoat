/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.idor

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
    "idor.hints.idorDiffAttributes1",
    "idor.hints.idorDiffAttributes2",
    "idor.hints.idorDiffAttributes3",
)
class IDORDiffAttributes : AssignmentEndpoint {
    @PostMapping("/IDOR/diff-attributes")
    @ResponseBody
    fun completed(
        @RequestParam attributes: String,
    ): AttackResult {
        val trimmedAttributes = attributes.trim()
        val diffAttribs = trimmedAttributes.split(",")

        if (diffAttribs.size < 2) {
            return failed(this).feedback("idor.diff.attributes.missing").build()
        }

        val first = diffAttribs[0].lowercase().trim()
        val second = diffAttribs[1].lowercase().trim()

        return if ((first == "userid" && second == "role") || (first == "role" && second == "userid")) {
            success(this).feedback("idor.diff.success").build()
        } else {
            failed(this).feedback("idor.diff.failure").build()
        }
    }
}
