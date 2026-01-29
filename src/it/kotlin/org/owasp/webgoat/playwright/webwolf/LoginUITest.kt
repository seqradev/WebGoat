/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webwolf

import com.microsoft.playwright.Browser
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication
import org.owasp.webgoat.playwright.webwolf.pages.WebWolfLoginPage

class LoginUITest : PlaywrightTest() {
    @Test
    fun login(browser: Browser) {
        val page = Authentication.tweety(browser)
        val loginPage = WebWolfLoginPage(page)
        loginPage.open()
        loginPage.login(Authentication.getTweety().name, Authentication.getTweety().password)

        assertThat(loginPage.signInButton).not().isVisible()

        // logout
        loginPage.logout()

        assertThat(loginPage.signInButton).isVisible()
    }
}
