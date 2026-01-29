/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import org.junit.jupiter.api.Test

class SSRFIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        startLesson("SSRF")

        var params = mapOf<String, Any>("url" to "images/jerry.png")
        checkAssignment(webGoatUrlConfig.url("SSRF/task1"), params, true)

        params = mapOf("url" to "http://ifconfig.pro")
        checkAssignment(webGoatUrlConfig.url("SSRF/task2"), params, true)

        checkResults("SSRF")
    }
}
