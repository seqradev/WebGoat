/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

data class User(
    var username: String = "",
    var password: String = "",
    var isAdmin: Boolean = false,
)
