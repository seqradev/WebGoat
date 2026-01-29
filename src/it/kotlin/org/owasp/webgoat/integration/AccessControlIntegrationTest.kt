/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test

class AccessControlIntegrationTest : IntegrationTest() {
    @Test
    fun testLesson() {
        startLesson("MissingFunctionAC", true)
        assignment1()
        assignment2()
        assignment3()

        checkResults("MissingFunctionAC")
    }

    private fun assignment3() {
        // direct call should fail if user has not been created
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .contentType(ContentType.JSON)
            .get(webGoatUrlConfig.url("access-control/users-admin-fix"))
            .then()
            .statusCode(HttpStatus.SC_FORBIDDEN)

        // create user
        val userTemplate = """{"username":"%s","password":"%s","admin": "true"}"""
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .contentType(ContentType.JSON)
            .body(userTemplate.format(user, user))
            .post(webGoatUrlConfig.url("access-control/users"))
            .then()
            .statusCode(HttpStatus.SC_OK)

        // get the users
        val userHash =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .contentType(ContentType.JSON)
                .get(webGoatUrlConfig.url("access-control/users-admin-fix"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get<String>("find { it.username == \"Jerry\" }.userHash")

        checkAssignment(
            webGoatUrlConfig.url("access-control/user-hash-fix"),
            mapOf("userHash" to userHash),
            true,
        )
    }

    private fun assignment2() {
        val userHash =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .contentType(ContentType.JSON)
                .get(webGoatUrlConfig.url("access-control/users"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get<String>("find { it.username == \"Jerry\" }.userHash")

        checkAssignment(
            webGoatUrlConfig.url("access-control/user-hash"),
            mapOf("userHash" to userHash),
            true,
        )
    }

    private fun assignment1() {
        val params = mapOf("hiddenMenu1" to "Users", "hiddenMenu2" to "Config")
        checkAssignment(webGoatUrlConfig.url("access-control/hidden-menu"), params, true)
    }
}
