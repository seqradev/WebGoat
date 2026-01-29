/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_SIMPLE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "access-control.hash.hint1",
    "access-control.hash.hint2",
    "access-control.hash.hint3",
    "access-control.hash.hint4",
    "access-control.hash.hint5",
)
class MissingFunctionACYourHash(
    private val userRepository: MissingAccessControlUserRepository,
) : AssignmentEndpoint {
    @PostMapping(path = ["/access-control/user-hash"], produces = ["application/json"])
    @ResponseBody
    fun simple(userHash: String): AttackResult {
        val user = requireNotNull(userRepository.findByUsername("Jerry")) { "User Jerry not found" }
        val displayUser = DisplayUser(user, PASSWORD_SALT_SIMPLE)
        return if (userHash == displayUser.userHash) {
            success(this).feedback("access-control.hash.success").build()
        } else {
            failed(this).build()
        }
    }
}
