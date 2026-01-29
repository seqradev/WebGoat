/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.idor

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints(
    "idor.hints.ownProfileAltUrl1",
    "idor.hints.ownProfileAltUrl2",
    "idor.hints.ownProfileAltUrl3",
)
class IDORViewOwnProfileAltUrl(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @PostMapping("/IDOR/profile/alt-path")
    @ResponseBody
    fun completed(
        @RequestParam url: String,
    ): AttackResult =
        try {
            if (userSessionData.getValue("idor-authenticated-as") == "tom") {
                // going to use session auth to view this one
                val authUserId =
                    requireNotNull(userSessionData.getValue("idor-authenticated-user-id") as? String) {
                        "User ID must be set"
                    }
                // don't care about http://localhost:8080 ... just want WebGoat/
                val urlParts = url.split("/")
                if (urlParts[0] == "WebGoat" &&
                    urlParts[1] == "IDOR" &&
                    urlParts[2] == "profile" &&
                    urlParts[3] == authUserId
                ) {
                    val userProfile = UserProfile(authUserId)
                    success(this)
                        .feedback("idor.view.own.profile.success")
                        .output(userProfile.profileToMap().toString())
                        .build()
                } else {
                    failed(this).feedback("idor.view.own.profile.failure1").build()
                }
            } else {
                failed(this).feedback("idor.view.own.profile.failure2").build()
            }
        } catch (ex: Exception) {
            failed(this).output("an error occurred with your request").build()
        }
}
