/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest

class WebGoatLoginPage(
    private val page: Page,
) {
    val signInButton: Locator =
        page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Sign in"),
        )

    fun open() {
        page.navigate(PlaywrightTest.webGoatUrl("login"))
    }

    fun login(
        username: String,
        password: String,
    ) {
        page.getByPlaceholder("Username").fill(username)
        page.getByPlaceholder("Password").fill(password)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sign in")).click()
    }
}
