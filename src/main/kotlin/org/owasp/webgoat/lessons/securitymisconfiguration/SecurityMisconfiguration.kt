/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

/** Lesson entry point for Security Misconfiguration. */
@Component
class SecurityMisconfiguration : Lesson() {
    override fun getDefaultCategory(): Category = Category.A5

    override fun getTitle(): String = "securitymisconfiguration.title"
}
