/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.advanced

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class SqlInjectionAdvanced : Lesson() {
    override fun getDefaultCategory(): Category = Category.A3

    override fun getTitle(): String = "2.sql.advanced.title"
}
