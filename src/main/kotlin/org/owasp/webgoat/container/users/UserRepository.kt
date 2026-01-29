/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<WebGoatUser, String> {
    fun findByUsername(username: String): WebGoatUser?

    override fun findAll(): List<WebGoatUser>

    fun existsByUsername(username: String): Boolean
}
