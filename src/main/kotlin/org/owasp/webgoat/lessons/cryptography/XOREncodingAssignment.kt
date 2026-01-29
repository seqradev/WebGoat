/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

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
@AssignmentHints("crypto-encoding-xor.hints.1")
class XOREncodingAssignment : AssignmentEndpoint {
    @PostMapping("/crypto/encoding/xor")
    @ResponseBody
    fun completed(
        @RequestParam answer_pwd1: String?,
    ): AttackResult =
        if (answer_pwd1 != null && answer_pwd1 == "databasepassword") {
            success(this).feedback("crypto-encoding-xor.success").build()
        } else {
            failed(this).feedback("crypto-encoding-xor.empty").build()
        }
}
