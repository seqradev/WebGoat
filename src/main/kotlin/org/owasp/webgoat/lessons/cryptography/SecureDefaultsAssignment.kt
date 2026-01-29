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
@AssignmentHints(
    "crypto-secure-defaults.hints.1",
    "crypto-secure-defaults.hints.2",
    "crypto-secure-defaults.hints.3",
)
class SecureDefaultsAssignment : AssignmentEndpoint {
    @PostMapping("/crypto/secure/defaults")
    @ResponseBody
    fun completed(
        @RequestParam secretFileName: String?,
        @RequestParam secretText: String?,
    ): AttackResult {
        if (secretFileName != null && secretFileName == "default_secret") {
            if (secretText != null &&
                HashingAssignment
                    .getHash(secretText, "SHA-256")
                    .equals(
                        "34de66e5caf2cb69ff2bebdc1f3091ecf6296852446c718e38ebfa60e4aa75d2",
                        ignoreCase = true,
                    )
            ) {
                return success(this).feedback("crypto-secure-defaults.success").build()
            } else {
                return failed(this).feedback("crypto-secure-defaults.messagenotok").build()
            }
        }
        return failed(this).feedback("crypto-secure-defaults.notok").build()
    }
}
