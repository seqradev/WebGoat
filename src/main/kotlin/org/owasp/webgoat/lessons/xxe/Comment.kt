/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

@XmlRootElement(name = "comment")
@XmlType
class Comment(
    var user: String? = null,
    var dateTime: String? = null,
    var text: String? = null,
) {
    override fun toString(): String = "Comment(user=$user, dateTime=$dateTime, text=$text)"
}
