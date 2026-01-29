/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.springframework.data.annotation.Id

class UserSession(
    val webGoatUser: WebGoatUser,
    @Id val sessionId: String,
) {
    protected constructor() : this(WebGoatUser("", ""), "")
}
