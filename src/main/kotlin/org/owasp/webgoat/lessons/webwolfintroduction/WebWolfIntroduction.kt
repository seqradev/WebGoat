/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.webwolfintroduction

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class WebWolfIntroduction : Lesson() {
    override fun getDefaultCategory(): Category = Category.INTRODUCTION

    override fun getTitle(): String = "webwolf.title"
}
