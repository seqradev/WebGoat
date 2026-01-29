/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WebWolfIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        startLesson("WebWolfIntroduction")

        // Assignment 3
        var params = mutableMapOf<String, Any>("email" to "$user@webgoat.org")
        checkAssignment(webGoatUrlConfig.url("WebWolf/mail/send"), params, false)

        var responseBody =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("WEBWOLFSESSION", webWolfCookie)
                .get(webWolfUrlConfig.url("mail"))
                .then()
                .extract()
                .response()
                .body
                .asString()

        var uniqueCode = responseBody.replace("%20", " ")
        uniqueCode =
            uniqueCode.substring(
                21 + uniqueCode.lastIndexOf("your unique code is: "),
                uniqueCode.lastIndexOf("your unique code is: ") + (21 + user.length),
            )
        params.clear()
        params["uniqueCode"] = uniqueCode
        checkAssignment(webGoatUrlConfig.url("WebWolf/mail"), params, true)

        // Assignment 4
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .queryParams(params)
            .get(webGoatUrlConfig.url("WebWolf/landing/password-reset"))
            .then()
            .statusCode(200)
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("WEBWOLFSESSION", webWolfCookie)
            .queryParams(params)
            .get(webWolfUrlConfig.url("landing"))
            .then()
            .statusCode(200)
        responseBody =
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
        assertTrue(responseBody.contains(uniqueCode))
        params.clear()
        params["uniqueCode"] = uniqueCode
        checkAssignment(webGoatUrlConfig.url("WebWolf/landing"), params, true)

        checkResults("WebWolfIntroduction")
    }
}
