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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("idor.hints.idor_login")
class IDORLogin(
    private val lessonSession: LessonSession,
) : AssignmentEndpoint {
    private val idorUserInfo = mutableMapOf<String, MutableMap<String, String>>()

    private fun initIDORInfo() {
        idorUserInfo["tom"] =
            mutableMapOf(
                "password" to "cat",
                "id" to "2342384",
                "color" to "yellow",
                "size" to "small",
            )

        idorUserInfo["bill"] =
            mutableMapOf(
                "password" to "buffalo",
                "id" to "2342388",
                "color" to "brown",
                "size" to "large",
            )
    }

    @PostMapping("/IDOR/login")
    @ResponseBody
    fun completed(
        @RequestParam username: String,
        @RequestParam password: String,
    ): AttackResult {
        initIDORInfo()

        if (username !in idorUserInfo) {
            return failed(this).feedback("idor.login.failure").build()
        }

        return if (username == "tom" && idorUserInfo["tom"]?.get("password") == password) {
            lessonSession.setValue("idor-authenticated-as", username)
            lessonSession.setValue("idor-authenticated-user-id", idorUserInfo[username]?.get("id"))
            success(this).feedback("idor.login.success").feedbackArgs(username).build()
        } else {
            failed(this).feedback("idor.login.failure").build()
        }
    }
}
