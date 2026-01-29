/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Task exposing verbose stack traces leaking sensitive configuration. */
@RestController
@AssignmentHints(
    "securitymisconfiguration.task2.hint1",
    "securitymisconfiguration.task2.hint2",
)
class VerboseErrorTask : AssignmentEndpoint {
    @GetMapping(
        value = ["/SecurityMisconfiguration/task2/trigger"],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    fun triggerError(): ResponseEntity<String> {
        val stackTrace =
            "2025-03-21 09:42:11,012 ERROR [staging] com.webgoat.DebugController " +
                "- Null pointer while rendering template\n" +
                "java.lang.NullPointerException: Cannot invoke \"Object.toString()\" " +
                "because \"ctx\" is null\n" +
                "\tat com.webgoat.DebugController.render(DebugController.java:94)\n" +
                "\tat org.springframework.mvc.DispatcherServlet.doDispatch(" +
                "DispatcherServlet.java:1101)\n" +
                "\tat ...\n" +
                "\nENVIRONMENT=staging\n" +
                "DEBUG_MODE=true\n" +
                "DB_USER=staging_user\n" +
                "DB_PASSWORD=staging_password123\n" +
                "SYSTEM_API_TOKEN=$LEAKED_TOKEN\n"
        return ResponseEntity.ok(stackTrace)
    }

    @GetMapping(
        value = ["/SecurityMisconfiguration/task2/config"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun fetchConfig(
        @RequestParam(value = "token", required = false) token: String?,
    ): ResponseEntity<String> =
        if (LEAKED_TOKEN == token) {
            val json =
                """
                {
                  "feature": "debug",
                  "logging": "trace",
                  "notes": "Never expose this in production!"
                }
                """.trimIndent()
            ResponseEntity.ok(json)
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("ACCESS DENIED")
        }

    @PostMapping(
        value = ["/SecurityMisconfiguration/task2"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun submitToken(
        @RequestParam("token") token: String?,
    ): AttackResult =
        when {
            LEAKED_TOKEN == token ->
                success(this)
                    .feedback("securitymisconfiguration.task2.success")
                    .output("Debug mode disabled. Stack traces are now safe for users.")
                    .build()
            token.isNullOrBlank() ->
                failed(this)
                    .feedback("securitymisconfiguration.task2.failure.blank")
                    .build()
            else ->
                failed(this)
                    .feedback("securitymisconfiguration.task2.failure.invalid")
                    .build()
        }

    companion object {
        const val LEAKED_TOKEN = "STAGING-TOKEN-42"
    }
}
