/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigHardeningTaskTest {
    private lateinit var task: ConfigHardeningTask

    @BeforeEach
    fun setUp() {
        task = ConfigHardeningTask()
    }

    @Test
    fun shouldFailWhenAnySettingIncorrect() {
        val result = task.submitConfig("true", "never", "admin", "password")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task4.failure.invalid")
    }

    @Test
    fun shouldPassWhenSettingsMatchHardenedValues() {
        val result = task.submitConfig("false", "never", "", "")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task4.success")
    }
}
