/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class MissingFunctionAC : Lesson() {
    override fun getDefaultCategory(): Category = Category.A1

    override fun getTitle(): String = "missing-function-access-control.title"

    companion object {
        const val PASSWORD_SALT_SIMPLE = "DeliberatelyInsecure1234"
        const val PASSWORD_SALT_ADMIN = "DeliberatelyInsecure1235"
    }
}
