/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import jakarta.validation.constraints.NotBlank
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Task where learners harden a configuration by disabling insecure settings. */
@RestController
@AssignmentHints(
    "securitymisconfiguration.task4.hint1",
    "securitymisconfiguration.task4.hint2",
)
class ConfigHardeningTask : AssignmentEndpoint {
    @PostMapping(
        value = ["/SecurityMisconfiguration/task4"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun submitConfig(
        @RequestParam("envEnabled") @NotBlank envEnabled: String,
        @RequestParam("healthDetails") @NotBlank healthDetails: String,
        @RequestParam(value = "defaultUser", required = false) defaultUser: String?,
        @RequestParam(value = "defaultPassword", required = false) defaultPassword: String?,
    ): AttackResult {
        val current =
            mapOf(
                "management.endpoint.env.enabled" to envEnabled.trim(),
                "management.endpoint.health.show-details" to healthDetails.trim(),
                "spring.security.user.name" to (defaultUser?.trim() ?: ""),
                "spring.security.user.password" to (defaultPassword?.trim() ?: ""),
            )

        return if (current == EXPECTED) {
            success(this)
                .feedback("securitymisconfiguration.task4.success")
                .output("Configuration hardened: debug endpoints disabled, default user removed.")
                .build()
        } else {
            failed(this)
                .feedback("securitymisconfiguration.task4.failure.invalid")
                .output("Check that env endpoint is disabled, health details hidden, and default user removed.")
                .build()
        }
    }

    companion object {
        private val EXPECTED =
            mapOf(
                "management.endpoint.env.enabled" to "false",
                "management.endpoint.health.show-details" to "never",
                "spring.security.user.name" to "",
                "spring.security.user.password" to "",
            )
    }
}
