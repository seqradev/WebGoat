/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import org.junit.jupiter.api.Test

class SessionManagementIT : IntegrationTest() {
    companion object {
        private const val HIJACK_LOGIN_CONTEXT_PATH = "HijackSession/login"
    }

    @Test
    fun hijackSessionTest() {
        startLesson("HijackSession")

        checkAssignment(
            webGoatUrlConfig.url(HIJACK_LOGIN_CONTEXT_PATH),
            mapOf("username" to "webgoat", "password" to "webgoat"),
            false,
        )
    }
}
