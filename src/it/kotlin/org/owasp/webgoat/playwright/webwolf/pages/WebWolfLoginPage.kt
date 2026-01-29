/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webwolf.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest

class WebWolfLoginPage(
    private val page: Page,
) {
    val signInButton: Locator =
        page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Sign In"),
        )
    private val signOutButton: Locator =
        page.getByRole(
            AriaRole.LINK,
            Page.GetByRoleOptions().setName("Sign out"),
        )

    fun open() {
        page.navigate(PlaywrightTest.webWolfURL("login"))
    }

    fun login(
        username: String,
        password: String,
    ) {
        page.getByPlaceholder("Username WebGoat").fill(username)
        page.getByPlaceholder("Password WebGoat").fill(password)
        signInButton.click()
    }

    fun logout() {
        signOutButton.click()
    }
}
