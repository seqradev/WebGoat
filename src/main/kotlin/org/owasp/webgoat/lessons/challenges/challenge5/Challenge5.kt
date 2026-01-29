/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge5

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class Challenge5 : Lesson() {
    override fun getDefaultCategory(): Category = Category.CHALLENGE

    override fun getTitle(): String = "challenge5.title"
}
