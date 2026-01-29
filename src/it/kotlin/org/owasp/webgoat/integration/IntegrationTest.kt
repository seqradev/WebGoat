/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.owasp.webgoat.ServerUrlConfig
import org.springframework.http.HttpStatus

abstract class IntegrationTest {
    protected val webGoatUrlConfig: ServerUrlConfig = ServerUrlConfig.webGoat()
    protected val webWolfUrlConfig: ServerUrlConfig = ServerUrlConfig.webWolf()

    var webGoatCookie: String = ""
        private set
    var webWolfCookie: String = ""
        private set
    val user: String = "webgoat"

    @BeforeEach
    fun login() {
        login("webgoat")
    }

    protected fun login(user: String) {
        val location =
            given()
                .`when`()
                .relaxedHTTPSValidation()
                .formParam("username", user)
                .formParam("password", "password")
                .post(webGoatUrlConfig.url("login"))
                .then()
                .log()
                .ifValidationFails(LogDetail.ALL)
                .cookie("JSESSIONID")
                .statusCode(302)
                .extract()
                .header("Location")

        webGoatCookie =
            if (location.endsWith("?error")) {
                RestAssured
                    .given()
                    .`when`()
                    .relaxedHTTPSValidation()
                    .formParam("username", user)
                    .formParam("password", "password")
                    .formParam("matchingPassword", "password")
                    .formParam("agree", "agree")
                    .post(webGoatUrlConfig.url("register.mvc"))
                    .then()
                    .cookie("JSESSIONID")
                    .statusCode(302)
                    .extract()
                    .cookie("JSESSIONID")
            } else {
                given()
                    .`when`()
                    .relaxedHTTPSValidation()
                    .formParam("username", user)
                    .formParam("password", "password")
                    .post(webGoatUrlConfig.url("login"))
                    .then()
                    .cookie("JSESSIONID")
                    .statusCode(302)
                    .extract()
                    .cookie("JSESSIONID")
            }

        webWolfCookie =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .formParam("username", user)
                .formParam("password", "password")
                .post(webWolfUrlConfig.url("login"))
                .then()
                .statusCode(302)
                .cookie("WEBWOLFSESSION")
                .extract()
                .cookie("WEBWOLFSESSION")
    }

    @AfterEach
    fun logout() {
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .get(webGoatUrlConfig.url("logout"))
            .then()
            .statusCode(200)
    }

    fun startLesson(lessonName: String) {
        startLesson(lessonName, false)
    }

    fun startLesson(
        lessonName: String,
        restart: Boolean,
    ) {
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .get(webGoatUrlConfig.url("$lessonName.lesson.lesson"))
            .then()
            .statusCode(200)

        if (restart) {
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/restartlesson.mvc/$lessonName.lesson"))
                .then()
                .statusCode(200)
        }
    }

    fun checkAssignment(
        url: String,
        params: Map<String, *>,
        expectedResult: Boolean,
    ) {
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .post(url)
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(expectedResult),
        )
    }

    fun checkAssignmentWithPUT(
        url: String,
        params: Map<String, *>,
        expectedResult: Boolean,
    ) {
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .put(url)
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(expectedResult),
        )
    }

    fun checkResults(lesson: String) {
        val result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/lessonoverview.mvc/$lesson.lesson"))
                .andReturn()

        MatcherAssert.assertThat(
            result
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList<Boolean>("solved"),
            CoreMatchers.everyItem(CoreMatchers.`is`(true)),
        )
    }

    fun checkResults() {
        val result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/lessonoverview.mvc"))
                .andReturn()

        MatcherAssert.assertThat(
            result
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList<Boolean>("solved"),
            CoreMatchers.everyItem(CoreMatchers.`is`(true)),
        )
    }

    fun checkAssignment(
        url: String,
        contentType: ContentType,
        body: String,
        expectedResult: Boolean,
    ) {
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(contentType)
                .cookie("JSESSIONID", webGoatCookie)
                .body(body)
                .post(url)
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(expectedResult),
        )
    }

    fun checkAssignmentWithGet(
        url: String,
        params: Map<String, *>,
        expectedResult: Boolean,
    ) {
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .queryParams(params)
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(expectedResult),
        )
    }

    fun getWebWolfFileServerLocation(): String {
        var result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("WEBWOLFSESSION", webWolfCookie)
                .get(webWolfUrlConfig.url("file-server-location"))
                .then()
                .extract()
                .response()
                .body
                .asString()
        result = result.replace("%20", " ")
        return result
    }

    fun webGoatServerDirectory(): String =
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .get(webGoatUrlConfig.url("server-directory"))
            .then()
            .extract()
            .response()
            .body
            .asString()

    fun cleanMailbox() {
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("WEBWOLFSESSION", webWolfCookie)
            .delete(webWolfUrlConfig.url("mail"))
            .then()
            .statusCode(HttpStatus.ACCEPTED.value())
    }
}
