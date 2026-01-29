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
class BypassRestrictionsFrontendValidation : AssignmentEndpoint {
    @PostMapping("/BypassRestrictions/frontendValidation")
    @ResponseBody
    fun completed(
        @RequestParam field1: String,
        @RequestParam field2: String,
        @RequestParam field3: String,
        @RequestParam field4: String,
        @RequestParam field5: String,
        @RequestParam field6: String,
        @RequestParam field7: String,
        @RequestParam error: Int,
    ): AttackResult {
        val regex1 = "^[a-z]{3}$"
        val regex2 = "^[0-9]{3}$"
        val regex3 = "^[a-zA-Z0-9 ]*$"
        val regex4 = "^(one|two|three|four|five|six|seven|eight|nine)$"
        val regex5 = "^\\d{5}$"
        val regex6 = "^\\d{5}(-\\d{4})?$"
        val regex7 = "^[2-9]\\d{2}-?\\d{3}-?\\d{4}$"
        if (error > 0) {
            return failed(this).build()
        }
        if (field1.matches(Regex(regex1))) {
            return failed(this).build()
        }
        if (field2.matches(Regex(regex2))) {
            return failed(this).build()
        }
        if (field3.matches(Regex(regex3))) {
            return failed(this).build()
        }
        if (field4.matches(Regex(regex4))) {
            return failed(this).build()
        }
        if (field5.matches(Regex(regex5))) {
            return failed(this).build()
        }
        if (field6.matches(Regex(regex6))) {
            return failed(this).build()
        }
        if (field7.matches(Regex(regex7))) {
            return failed(this).build()
        }
        return success(this).build()
    }
}
