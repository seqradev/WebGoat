/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class IDORIntegrationTest : IntegrationTest() {
    @BeforeEach
    fun init() {
        startLesson("IDOR")
    }

    @TestFactory
    fun testIDORLesson(): Iterable<DynamicTest> =
        listOf(
            dynamicTest("assignment 2 - login", this::loginIDOR),
            dynamicTest("profile", this::profile),
        )

    @AfterEach
    fun shutdown() {
        checkResults("IDOR")
    }

    private fun loginIDOR() {
        val params =
            mapOf<String, Any>(
                "username" to "tom",
                "password" to "cat",
            )
        checkAssignment(webGoatUrlConfig.url("IDOR/login"), params, true)
    }

    private fun profile() {
        // View profile - assignment 3a
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("IDOR/profile"))
                .then()
                .statusCode(200)
                .extract()
                .path("userId"),
            `is`("2342384"),
        )

        // Show difference - assignment 3b
        var params = mapOf<String, Any>("attributes" to "userId,role")
        checkAssignment(webGoatUrlConfig.url("IDOR/diff-attributes"), params, true)

        // View profile another way - assignment 4
        params = mapOf("url" to "WebGoat/IDOR/profile/2342384")
        checkAssignment(webGoatUrlConfig.url("IDOR/profile/alt-path"), params, true)

        // assignment 5a
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("IDOR/profile/2342388"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )

        // assignment 5b
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .contentType(ContentType.JSON) // part of the lesson
                .body(
                    """{"role":"1", "color":"red", "size":"large", "name":"Buffalo Bill", "userId":"2342388"}""",
                ).put(webGoatUrlConfig.url("IDOR/profile/2342388"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )
    }
}
