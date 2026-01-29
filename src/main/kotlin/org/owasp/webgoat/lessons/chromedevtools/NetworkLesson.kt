/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.chromedevtools

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Assignment where the user has to look through an HTTP Request using the Developer Tools and find
 * a specific number.
 */
@RestController
@AssignmentHints("networkHint1", "networkHint2")
class NetworkLesson : AssignmentEndpoint {
    @PostMapping(value = ["/ChromeDevTools/network"], params = ["network_num", "number"])
    @ResponseBody
    fun completed(
        @RequestParam network_num: String,
        @RequestParam number: String,
    ): AttackResult =
        if (network_num == number) {
            success(this).feedback("network.success").output("").build()
        } else {
            failed(this).feedback("network.failed").build()
        }

    @PostMapping(path = ["/ChromeDevTools/network"], params = ["networkNum"])
    @ResponseBody
    fun ok(
        @RequestParam networkNum: String,
    ): ResponseEntity<*> = ResponseEntity.ok<Any?>(null)
}
