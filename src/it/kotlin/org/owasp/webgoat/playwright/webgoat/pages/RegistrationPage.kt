/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest

class RegistrationPage(
    private val page: Page,
) {
    val signUpButton: Locator =
        page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Sign up"),
        )

    fun open() {
        page.navigate(PlaywrightTest.webGoatUrl("registration"))
    }

    fun register(
        username: String,
        password: String,
    ) {
        page.getByPlaceholder("Username").fill(username)
        page.getByLabel("Password", Page.GetByLabelOptions().setExact(true)).fill(password)
        page.getByLabel("Confirm password").fill(password)
        page.getByLabel("Agree with the terms and").check()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sign up")).click()
    }
}
