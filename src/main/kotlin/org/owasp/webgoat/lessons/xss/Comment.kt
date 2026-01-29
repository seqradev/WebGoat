/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement
data class Comment(
    var user: String? = null,
    var dateTime: String? = null,
    var text: String? = null,
)
