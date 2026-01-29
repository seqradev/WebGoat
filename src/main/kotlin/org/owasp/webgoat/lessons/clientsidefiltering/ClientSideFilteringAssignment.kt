/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

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
    "ClientSideFilteringHint1",
    "ClientSideFilteringHint2",
    "ClientSideFilteringHint3",
    "ClientSideFilteringHint4",
)
class ClientSideFilteringAssignment : AssignmentEndpoint {
    @PostMapping("/clientSideFiltering/attack1")
    @ResponseBody
    fun completed(
        @RequestParam answer: String,
    ): AttackResult =
        if (answer == "450000") {
            success(this).feedback("assignment.solved").build()
        } else {
            failed(this).feedback("ClientSideFiltering.incorrect").build()
        }
}
