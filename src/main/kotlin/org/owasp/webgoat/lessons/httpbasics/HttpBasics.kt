/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.httpbasics

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class HttpBasics : Lesson() {
    override fun getDefaultCategory(): Category = Category.GENERAL

    override fun getTitle(): String = "1.http-basics.title" // first lesson in general
}
