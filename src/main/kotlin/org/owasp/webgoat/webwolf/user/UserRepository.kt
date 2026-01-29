/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("webWolfUserRepository")
interface UserRepository : JpaRepository<WebWolfUser, String> {
    fun findByUsername(username: String): WebWolfUser?
}
