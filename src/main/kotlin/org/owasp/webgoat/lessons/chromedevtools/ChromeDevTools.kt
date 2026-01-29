/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.chromedevtools

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class ChromeDevTools : Lesson() {
    override fun getDefaultCategory(): Category = Category.GENERAL

    override fun getTitle(): String = "3.chrome-dev-tools.title" // 3rd lesson in General
}
