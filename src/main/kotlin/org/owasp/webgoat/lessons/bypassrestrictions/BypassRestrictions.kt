/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.bypassrestrictions

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class BypassRestrictions : Lesson() {
    override fun getDefaultCategory(): Category = Category.CLIENT_SIDE

    override fun getTitle(): String = "bypass-restrictions.title"
}
