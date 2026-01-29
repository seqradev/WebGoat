/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class FlagController(
    private val flags: Flags,
) : AssignmentEndpoint {
    @PostMapping(path = ["/challenge/flag/{flagNumber}"])
    @ResponseBody
    fun postFlag(
        @PathVariable flagNumber: Int,
        @RequestParam flag: String,
    ): AttackResult {
        val expectedFlag = flags.getFlag(flagNumber)
        return if (expectedFlag?.isCorrect(flag) == true) {
            success(this).feedback("challenge.flag.correct").build()
        } else {
            failed(this).feedback("challenge.flag.incorrect").build()
        }
    }
}
