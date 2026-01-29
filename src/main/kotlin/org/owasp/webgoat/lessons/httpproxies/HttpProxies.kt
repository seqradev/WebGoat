/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.httpproxies

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class HttpProxies : Lesson() {
    override fun getDefaultCategory(): Category = Category.GENERAL

    override fun getTitle(): String = "2.http-proxies.title" // second lesson in GENERAL
}
