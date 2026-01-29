/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
class User(
    var username: String = "",
    var password: String = "",
)
