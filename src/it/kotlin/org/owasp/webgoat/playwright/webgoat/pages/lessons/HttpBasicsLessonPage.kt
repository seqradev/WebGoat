/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages.lessons

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

class HttpBasicsLessonPage(
    page: Page,
) : LessonPage(page) {
    val enterYourName: Locator = page.locator("input[name=\"person\"]")
    val goButton: Locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go!"))

    fun getTitle(): Locator =
        page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("HTTP Basics"),
        )
}
