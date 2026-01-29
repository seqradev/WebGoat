/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_SIMPLE

class DisplayUserTest {
    @Test
    fun testDisplayUserCreation() {
        val displayUser = DisplayUser(User("user1", "password1", true), PASSWORD_SALT_SIMPLE)
        assertThat(displayUser.isAdmin).isTrue()
    }

    @Test
    fun testDisplayUserHash() {
        val displayUser = DisplayUser(User("user1", "password1", false), PASSWORD_SALT_SIMPLE)
        assertThat(displayUser.userHash).isEqualTo("cplTjehjI/e5ajqTxWaXhU5NW9UotJfXj+gcbPvfWWc=")
    }
}
