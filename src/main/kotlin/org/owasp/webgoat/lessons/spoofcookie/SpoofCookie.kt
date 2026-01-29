/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.spoofcookie

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class SpoofCookie : Lesson() {
    override fun getDefaultCategory(): Category = Category.A1

    override fun getTitle(): String = "spoofcookie.title"
}
