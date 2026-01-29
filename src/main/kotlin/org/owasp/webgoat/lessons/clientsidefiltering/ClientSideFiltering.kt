/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.stereotype.Component

@Component
class ClientSideFiltering : Lesson() {
    override fun getDefaultCategory(): Category = Category.CLIENT_SIDE

    override fun getTitle(): String = "client.side.filtering.title"
}
