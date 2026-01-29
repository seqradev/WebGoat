/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

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
    "client.side.filtering.free.hint1",
    "client.side.filtering.free.hint2",
    "client.side.filtering.free.hint3",
)
class ClientSideFilteringFreeAssignment : AssignmentEndpoint {
    @PostMapping("/clientSideFiltering/getItForFree")
    @ResponseBody
    fun completed(
        @RequestParam checkoutCode: String,
    ): AttackResult =
        if (SUPER_COUPON_CODE == checkoutCode) {
            success(this).build()
        } else {
            failed(this).build()
        }

    companion object {
        const val SUPER_COUPON_CODE = "get_it_for_free"
    }
}
