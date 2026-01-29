/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge1

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.challenges.Flags
import org.owasp.webgoat.lessons.challenges.SolutionConstants.PASSWORD
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Assignment1(
    private val flags: Flags,
) : AssignmentEndpoint {
    @PostMapping("/challenge/1")
    @ResponseBody
    fun completed(
        @RequestParam username: String,
        @RequestParam password: String,
    ): AttackResult {
        val ipAddressKnown = true
        val passwordCorrect =
            "admin" == username &&
                PASSWORD.replace("1234", String.format("%04d", ImageServlet.PINCODE)) == password
        return when {
            passwordCorrect && ipAddressKnown ->
                success(this).feedback("challenge.solved").feedbackArgs(flags.getFlag(1)).build()
            passwordCorrect ->
                failed(this).feedback("ip.address.unknown").build()
            else ->
                failed(this).build()
        }
    }
}
