/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class VerboseErrorTaskTest {
    private lateinit var task: VerboseErrorTask

    @BeforeEach
    fun setUp() {
        task = VerboseErrorTask()
    }

    @Test
    fun triggerShouldLeakTokenInStackTrace() {
        val response = task.triggerError()

        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).contains(VerboseErrorTask.LEAKED_TOKEN)
    }

    @Test
    fun shouldFailWhenTokenMissing() {
        val result = task.submitToken("")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task2.failure.blank")
    }

    @Test
    fun shouldFailWithIncorrectToken() {
        val result = task.submitToken("WRONG")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task2.failure.invalid")
    }

    @Test
    fun configEndpointShouldRequireToken() {
        val response = task.fetchConfig(null)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun configEndpointShouldReturnConfigWhenTokenMatches() {
        val response = task.fetchConfig(VerboseErrorTask.LEAKED_TOKEN)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).contains("debug")
    }

    @Test
    fun shouldPassWhenCorrectTokenProvided() {
        val result = task.submitToken(VerboseErrorTask.LEAKED_TOKEN)

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task2.success")
    }
}
