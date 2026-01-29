/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import org.dummy.insecure.framework.VulnerableTaskHolder
import org.junit.jupiter.api.Test
import org.owasp.webgoat.lessons.deserialization.SerializationHelper

class DeserializationIntegrationTest : IntegrationTest() {
    companion object {
        private val OS = System.getProperty("os.name").lowercase()
    }

    @Test
    fun runTests() {
        startLesson("InsecureDeserialization")

        val params = mutableMapOf<String, Any>()

        if (OS.contains("win")) {
            params["token"] =
                SerializationHelper.toString(VulnerableTaskHolder("wait", "ping localhost -n 5"))
        } else {
            params["token"] =
                SerializationHelper.toString(VulnerableTaskHolder("wait", "sleep 5"))
        }
        checkAssignment(webGoatUrlConfig.url("InsecureDeserialization/task"), params, true)

        checkResults("InsecureDeserialization")
    }
}
