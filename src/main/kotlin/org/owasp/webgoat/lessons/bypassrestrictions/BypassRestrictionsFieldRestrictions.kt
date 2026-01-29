/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.bypassrestrictions

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BypassRestrictionsFieldRestrictions : AssignmentEndpoint {
    @PostMapping("/BypassRestrictions/FieldRestrictions")
    @ResponseBody
    fun completed(
        @RequestParam select: String,
        @RequestParam radio: String,
        @RequestParam checkbox: String,
        @RequestParam shortInput: String,
        @RequestParam readOnlyInput: String,
    ): AttackResult {
        if (select == "option1" || select == "option2") {
            return failed(this).build()
        }
        if (radio == "option1" || radio == "option2") {
            return failed(this).build()
        }
        if (checkbox == "on" || checkbox == "off") {
            return failed(this).build()
        }
        if (shortInput.length <= 5) {
            return failed(this).build()
        }
        if ("change" == readOnlyInput) {
            return failed(this).build()
        }
        return success(this).build()
    }
}
