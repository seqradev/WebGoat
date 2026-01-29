/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.lessons

import com.microsoft.playwright.Browser
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication
import org.owasp.webgoat.playwright.webgoat.pages.lessons.SecurityMisconfigurationLessonPage

class SecurityMisconfigurationLessonUITest : PlaywrightTest() {
    private lateinit var lessonPage: SecurityMisconfigurationLessonPage

    @BeforeEach
    fun navigateToLesson(browser: Browser) {
        val lessonName = LessonName("SecurityMisconfiguration")
        val page = Authentication.sylvester(browser)

        lessonPage = SecurityMisconfigurationLessonPage(page)
        lessonPage.resetLesson(lessonName)
        lessonPage.open(lessonName)
    }

    @Test
    @DisplayName("Walk through Security Misconfiguration lesson")
    fun shouldCompleteSecurityMisconfigurationTasks() {
        // Task 1 - default credentials
        lessonPage.navigateTo(2)
        lessonPage.fillDefaultCredentials("admin", "admin")
        lessonPage.submitTask1()
        assertThat(lessonPage.task1Output()).containsText("Default admin account compromised")

        // Task 2 - verbose error leak
        lessonPage.navigateTo(3)

        lessonPage.triggerDebugLeak()
        Assertions.assertThat(lessonPage.debugOutput()).contains("SYSTEM_API_TOKEN")
        lessonPage.submitTask2(lessonPage.extractTokenFromDebug())
        assertThat(lessonPage.assignmentOutput).containsText("Debug token recovered")

        // Task 3 - actuator exposure
        lessonPage.navigateTo(4)

        val envJson = lessonPage.requestActuatorEnv()
        Assertions.assertThat(envJson).contains("systemApiKey")
        lessonPage.submitTask3(lessonPage.extractApiKey(envJson))
        assertThat(lessonPage.assignmentOutput).containsText("Actuator endpoints secured")

        // Task 4 - configuration hardening
        lessonPage.navigateTo(5)
        lessonPage.applyHardeningConfig()
        assertThat(lessonPage.assignmentOutput).containsText("Configuration hardened")
    }
}
