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
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_ADMIN
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "access-control.hash.hint6",
    "access-control.hash.hint7",
    "access-control.hash.hint8",
    "access-control.hash.hint9",
    "access-control.hash.hint10",
    "access-control.hash.hint11",
    "access-control.hash.hint12",
    "access-control.hash.hint13",
)
class MissingFunctionACYourHashAdmin(
    private val userRepository: MissingAccessControlUserRepository,
) : AssignmentEndpoint {
    @PostMapping(path = ["/access-control/user-hash-fix"], produces = ["application/json"])
    @ResponseBody
    fun admin(userHash: String): AttackResult {
        // current user should be in the DB
        // if not admin then return 403
        val user = requireNotNull(userRepository.findByUsername("Jerry")) { "User Jerry not found" }
        val displayUser = DisplayUser(user, PASSWORD_SALT_ADMIN)
        return if (userHash == displayUser.userHash) {
            success(this).feedback("access-control.hash.success").build()
        } else {
            failed(this).feedback("access-control.hash.close").build()
        }
    }
}
