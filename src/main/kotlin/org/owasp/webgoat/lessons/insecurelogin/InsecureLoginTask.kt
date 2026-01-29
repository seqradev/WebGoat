/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.insecurelogin

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class InsecureLoginTask : AssignmentEndpoint {
    @PostMapping("/InsecureLogin/task")
    @ResponseBody
    fun completed(
        @RequestParam username: String,
        @RequestParam password: String,
    ): AttackResult =
        if ("CaptainJack" == username && "BlackPearl" == password) {
            success(this).build()
        } else {
            failed(this).build()
        }

    @PostMapping("/InsecureLogin/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun login() {
        // only need to exists as the JS needs to call an existing endpoint
    }
}
