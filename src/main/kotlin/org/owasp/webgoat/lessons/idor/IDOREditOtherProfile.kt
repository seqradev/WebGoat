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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
class IDOREditOtherProfile(
    private val userSessionData: LessonSession,
) : AssignmentEndpoint {
    @PutMapping(path = ["/IDOR/profile/{userId}"], consumes = ["application/json"])
    @ResponseBody
    fun completed(
        @PathVariable("userId") userId: String,
        @RequestBody userSubmittedProfile: UserProfile,
    ): AttackResult {
        val authUserId = userSessionData.getValue("idor-authenticated-user-id") as? String
        // this is where it starts ... accepting the user submitted ID and assuming it will be the same
        // as the logged in userId and not checking for proper authorization
        // Certain roles can sometimes edit others' profiles, but we shouldn't just assume that and let
        // everyone, right?
        // Except that this is a vulnerable app ... so we will
        val currentUserProfile = UserProfile(userId)

        if (userSubmittedProfile.userId != null && userSubmittedProfile.userId != authUserId) {
            // let's get this started ...
            currentUserProfile.color = userSubmittedProfile.color
            currentUserProfile.role = userSubmittedProfile.role
            // we will persist in the session object for now in case we want to refer back or use it later
            userSessionData.setValue("idor-updated-other-profile", currentUserProfile)

            val isRed = currentUserProfile.color.equals("red", ignoreCase = true)
            return when {
                currentUserProfile.role <= 1 && isRed ->
                    success(this)
                        .feedback("idor.edit.profile.success1")
                        .output(currentUserProfile.profileToMap().toString())
                        .build()
                currentUserProfile.role > 1 && isRed ->
                    failed(this)
                        .feedback("idor.edit.profile.failure1")
                        .output(currentUserProfile.profileToMap().toString())
                        .build()
                currentUserProfile.role <= 1 && !isRed ->
                    failed(this)
                        .feedback("idor.edit.profile.failure2")
                        .output(currentUserProfile.profileToMap().toString())
                        .build()
                else ->
                    failed(this)
                        .feedback("idor.edit.profile.failure3")
                        .output(currentUserProfile.profileToMap().toString())
                        .build()
            }
        } else if (userSubmittedProfile.userId != null && userSubmittedProfile.userId == authUserId) {
            return failed(this).feedback("idor.edit.profile.failure4").build()
        }

        return if (currentUserProfile.color == "black" && currentUserProfile.role <= 1) {
            success(this)
                .feedback("idor.edit.profile.success2")
                .output(userSessionData.getValue("idor-updated-own-profile").toString())
                .build()
        } else {
            failed(this).feedback("idor.edit.profile.failure3").build()
        }
    }
}
