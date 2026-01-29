/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.idor

import org.owasp.webgoat.container.session.LessonSession
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class IDORViewOwnProfile(
    private val userSessionData: LessonSession,
) {
    private val log = LoggerFactory.getLogger(IDORViewOwnProfile::class.java)

    @GetMapping(
        path = ["/IDOR/own", "/IDOR/profile"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun invoke(): Map<String, Any?> {
        val details = mutableMapOf<String, Any?>()
        try {
            if (userSessionData.getValue("idor-authenticated-as") == "tom") {
                // going to use session auth to view this one
                val authUserId =
                    requireNotNull(userSessionData.getValue("idor-authenticated-user-id") as? String) {
                        "User ID must be set"
                    }
                val userProfile = UserProfile(authUserId)
                details["userId"] = userProfile.userId
                details["name"] = userProfile.name
                details["color"] = userProfile.color
                details["size"] = userProfile.size
                details["role"] = userProfile.role
            } else {
                details["error"] =
                    "You do not have privileges to view the profile. Authenticate as tom first please."
            }
        } catch (ex: Exception) {
            log.error("something went wrong: {}", ex.message)
        }
        return details
    }
}
