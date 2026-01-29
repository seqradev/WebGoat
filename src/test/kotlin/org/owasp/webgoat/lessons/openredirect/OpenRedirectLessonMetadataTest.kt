/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.lessons.Category

class OpenRedirectLessonMetadataTest {
    private val lesson = OpenRedirect()
    private val secureController = OpenRedirectSecureController()
    private val realRedirect = OpenRedirectRealRedirect()

    @Test
    fun lessonMetadataMatchesRegistration() {
        assertThat(lesson.getDefaultCategory()).isEqualTo(Category.GENERAL)
        assertThat(lesson.getTitle()).isEqualTo("openredirect.title")
    }

    @Test
    fun safeRedirectUsesMappedDestinationWhenKnown() {
        val response = secureController.safe(3)
        assertThat(response.viewName).isEqualTo("redirect:/logout")
    }

    @Test
    fun safeRedirectFallsBackToWelcomeWhenUnknownId() {
        val response = secureController.safe(99)
        assertThat(response.viewName).isEqualTo("redirect:/welcome.mvc")
    }

    @Test
    fun realRedirectReturnsRedirectPrefixForSuppliedUrl() {
        val response = realRedirect.real("https://attacker.example")
        assertThat(response.viewName).isEqualTo("redirect:https://attacker.example")
    }
}
