/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.springframework.data.jpa.repository.JpaRepository

interface UserProgressRepository : JpaRepository<UserProgress, String> {
    fun findByUser(user: String?): UserProgress?
}
