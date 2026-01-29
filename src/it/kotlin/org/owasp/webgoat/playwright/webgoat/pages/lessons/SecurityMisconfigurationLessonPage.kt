/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages.lessons

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest

class SecurityMisconfigurationLessonPage(
    page: Page,
) : LessonPage(page) {
    companion object {
        private val TOKEN_PATTERN = Regex("SYSTEM_API_TOKEN=([\\w-]+)")
        private val API_KEY_PATTERN = Regex("\"systemApiKey\"\\s*:\\s*\"([^\"]+)\"")
    }

    override fun open(lessonName: LessonName) {
        page.navigate(PlaywrightTest.webGoatUrl("start.mvc#lesson/${lessonName.lessonName()}"))
    }

    fun fillDefaultCredentials(
        username: String,
        password: String,
    ) {
        page.locator("[name='username']").fill(username)
        page.locator("[name='password']").fill(password)
    }

    fun submitTask1() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Attempt login")).click()
    }

    fun task1Output(): Locator = assignmentOutput

    fun triggerDebugLeak() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Trigger debug error")).click()
        page.waitForTimeout(500.0) // allow fetch to complete
    }

    fun debugOutput(): String = page.locator("#debug-output").textContent()

    fun extractTokenFromDebug(): String {
        val match = TOKEN_PATTERN.find(debugOutput())
        return match?.groupValues?.get(1) ?: ""
    }

    fun submitTask2(token: String) {
        page.getByLabel("Leaked token").fill(token)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit token")).click()
    }

    fun requestActuatorEnv(): String {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("GET /actuator/env")).click()
        page.waitForTimeout(300.0)
        return page.locator("#actuator-output").textContent()
    }

    fun extractApiKey(json: String): String {
        val match = API_KEY_PATTERN.find(json)
        return match?.groupValues?.get(1) ?: ""
    }

    fun submitTask3(apiKey: String) {
        page.getByLabel("System API key").fill(apiKey)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit key")).click()
    }

    fun task3Output(): Locator = page.locator(".lesson-page-wrapper").nth(3).locator(".attack-output")

    fun applyHardeningConfig() {
        page.getByLabel("management.endpoint.env.enabled").selectOption("false")
        page.getByLabel("management.endpoint.health.show-details").selectOption("never")
        page.getByLabel("spring.security.user.name").fill("")
        page.getByLabel("spring.security.user.password").fill("")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apply configuration")).click()
    }

    fun task4Output(): Locator = page.locator(".lesson-page-wrapper").locator(".attack-output").last()
}
