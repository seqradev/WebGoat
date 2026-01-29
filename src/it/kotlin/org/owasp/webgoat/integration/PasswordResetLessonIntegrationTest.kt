/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.http.HttpHeaders

class PasswordResetLessonIntegrationTest : IntegrationTest() {
    @BeforeEach
    fun init() {
        startLesson("PasswordReset")
    }

    @TestFactory
    fun passwordResetLesson(): Iterable<DynamicTest> =
        listOf(
            dynamicTest("assignment 6 - check email link") { sendEmailShouldBeAvailableInWebWolf() },
            dynamicTest("assignment 6 - solve assignment") { solveAssignment() },
            dynamicTest("assignment 2 - simple reset") { assignment2() },
            dynamicTest("assignment 4 - guess questions") { assignment4() },
            dynamicTest("assignment 5 - simple questions") { assignment5() },
        )

    fun assignment2() {
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/simple-mail/reset"),
            mapOf("emailReset" to "$user@webgoat.org"),
            false,
        )
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/simple-mail"),
            mapOf(
                "email" to "$user@webgoat.org",
                "password" to user.reversed(),
            ),
            true,
        )
    }

    fun assignment4() {
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/questions"),
            mapOf("username" to "tom", "securityQuestion" to "purple"),
            true,
        )
    }

    fun assignment5() {
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/SecurityQuestions"),
            mapOf("question" to "What is your favorite animal?"),
            false,
        )
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/SecurityQuestions"),
            mapOf("question" to "What is your favorite color?"),
            true,
        )
    }

    fun solveAssignment() {
        // WebGoat
        clickForgotEmailLink("tom@webgoat-cloud.org")

        // WebWolf
        val link = getPasswordResetLinkFromLandingPage()
        // WebGoat
        changePassword(link)
        checkAssignment(
            webGoatUrlConfig.url("PasswordReset/reset/login"),
            mapOf("email" to "tom@webgoat-cloud.org", "password" to "123456"),
            true,
        )
    }

    fun sendEmailShouldBeAvailableInWebWolf() {
        clickForgotEmailLink("$user@webgoat.org")

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
    }

    @AfterEach
    fun shutdown() {
        // this will run only once after the list of dynamic tests has run, this is to test if the
        // lesson is marked complete
        checkResults("PasswordReset")
    }

    private fun changePassword(link: String) {
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .formParams("resetLink", link, "password", "123456")
            .post(webGoatUrlConfig.url("PasswordReset/reset/change-password"))
            .then()
            .statusCode(200)
    }

    private fun getPasswordResetLinkFromLandingPage(): String {
        val responseBody =
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
        val startIndex = responseBody.lastIndexOf("/PasswordReset/reset/reset-password/")
        return responseBody.substring(
            startIndex + "/PasswordReset/reset/reset-password/".length,
            responseBody.indexOf(",", startIndex) - 1,
        )
    }

    private fun clickForgotEmailLink(user: String) {
        RestAssured
            .given()
            .`when`()
            .header(HttpHeaders.HOST, "${webWolfUrlConfig.host}:${webWolfUrlConfig.port}")
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .formParams("email", user)
            .post(webGoatUrlConfig.url("PasswordReset/ForgotPassword/create-password-reset-link"))
            .then()
            .statusCode(200)
    }
}
