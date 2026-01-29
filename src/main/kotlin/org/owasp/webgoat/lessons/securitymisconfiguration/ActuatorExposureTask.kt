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
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Task showing exposed actuator/admin endpoints leaking secrets. */
@RestController
@AssignmentHints(
    "securitymisconfiguration.task3.hint1",
    "securitymisconfiguration.task3.hint2",
)
class ActuatorExposureTask : AssignmentEndpoint {
    @GetMapping(
        value = ["/SecurityMisconfiguration/task3/actuator/env"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun actuatorEnv(): Map<String, Any> =
        mapOf(
            "name" to "webgoat-staging",
            "profiles" to arrayOf("staging", "debug"),
            "systemApiKey" to LEAKED_API_KEY,
            "features" to mapOf("betaUi" to true, "payments" to false),
        )

    @GetMapping(
        value = ["/SecurityMisconfiguration/task3/actuator/health"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun actuatorHealth(): Map<String, Any> =
        mapOf(
            "status" to "UP",
            "checks" to
                mapOf(
                    "database" to mapOf("status" to "UP", "responseTimeMs" to 12),
                    "cache" to mapOf("status" to "UP", "hitRatio" to 0.91),
                ),
        )

    @PostMapping(
        value = ["/SecurityMisconfiguration/task3"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun submitApiKey(
        @RequestParam("apiKey") apiKey: String?,
    ): AttackResult =
        when {
            LEAKED_API_KEY == apiKey ->
                success(this)
                    .feedback("securitymisconfiguration.task3.success")
                    .output("Actuator endpoints now require authentication and are limited to ops network.")
                    .build()
            apiKey.isNullOrBlank() ->
                failed(this)
                    .feedback("securitymisconfiguration.task3.failure.blank")
                    .build()
            else ->
                failed(this)
                    .feedback("securitymisconfiguration.task3.failure.invalid")
                    .build()
        }

    companion object {
        const val LEAKED_API_KEY = "INTERNAL-API-KEY-987"
    }
}
