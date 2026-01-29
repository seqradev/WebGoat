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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "access-control.hidden-menus.hint1",
    "access-control.hidden-menus.hint2",
    "access-control.hidden-menus.hint3",
)
class MissingFunctionACHiddenMenus : AssignmentEndpoint {
    @PostMapping(path = ["/access-control/hidden-menu"], produces = ["application/json"])
    @ResponseBody
    fun completed(
        hiddenMenu1: String,
        hiddenMenu2: String,
    ): AttackResult {
        if (hiddenMenu1 == "Users" && hiddenMenu2 == "Config") {
            return success(this).output("").feedback("access-control.hidden-menus.success").build()
        }

        if (hiddenMenu1 == "Config" && hiddenMenu2 == "Users") {
            return failed(this).output("").feedback("access-control.hidden-menus.close").build()
        }

        return failed(this).feedback("access-control.hidden-menus.failure").output("").build()
    }
}
