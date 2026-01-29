/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.idor

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "idor.hints.otherProfile1",
    "idor.hints.otherProfile2",
    "idor.hints.otherProfile3",
    "idor.hints.otherProfile4",
    "idor.hints.otherProfile5",
    "idor.hints.otherProfile6",
    "idor.hints.otherProfile7",
    "idor.hints.otherProfile8",
    "idor.hints.otherProfile9",
)
class IDORViewOtherProfile(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @GetMapping(
        path = ["/IDOR/profile/{userId}"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun completed(
        @PathVariable("userId") userId: String,
    ): AttackResult {
        if (userSessionData.getValue("idor-authenticated-as") != "tom") {
            return failed(this).build()
        }

        // going to use session auth to view this one
        val authUserId = userSessionData.getValue("idor-authenticated-user-id") as? String
        if (userId == authUserId) {
            return failed(this).feedback("idor.view.profile.close2").build()
        }

        // on the right track
        val requestedProfile = UserProfile(userId)
        // secure code would ensure there was a horizontal access control check prior to dishing up
        // the requested profile
        return if (requestedProfile.userId == "2342388") {
            success(this)
                .feedback("idor.view.profile.success")
                .output(requestedProfile.profileToMap().toString())
                .build()
        } else {
            failed(this).feedback("idor.view.profile.close1").build()
        }
    }
}
