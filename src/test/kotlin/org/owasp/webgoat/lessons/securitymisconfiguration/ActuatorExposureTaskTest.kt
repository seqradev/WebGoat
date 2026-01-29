/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActuatorExposureTaskTest {
    private lateinit var task: ActuatorExposureTask

    @BeforeEach
    fun setUp() {
        task = ActuatorExposureTask()
    }

    @Test
    fun envShouldExposeApiKey() {
        val response = task.actuatorEnv()
        assertThat(response["systemApiKey"]).isEqualTo(ActuatorExposureTask.LEAKED_API_KEY)
    }

    @Test
    fun healthShouldReturnStatus() {
        val response = task.actuatorHealth()
        assertThat(response["status"]).isEqualTo("UP")
    }

    @Test
    fun submitShouldFailWhenBlank() {
        val result = task.submitApiKey("")
        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task3.failure.blank")
    }

    @Test
    fun submitShouldFailWhenIncorrect() {
        val result = task.submitApiKey("WRONG")
        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task3.failure.invalid")
    }

    @Test
    fun submitShouldSucceedWithLeakedKey() {
        val result = task.submitApiKey(ActuatorExposureTask.LEAKED_API_KEY)
        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task3.success")
    }
}
