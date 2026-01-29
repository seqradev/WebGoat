/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        startLesson("OpenRedirect")

        val params = mutableMapOf<String, Any>()

        // Task 1: basic external URL
        params["url"] = "https://evil.test"
        checkAssignment(webGoatUrlConfig.url("OpenRedirect/task1"), params, true)

        // Task 2: naive substring filter bypass
        params.clear()
        params["url"] = "https://webgoat.org.evil.com"
        checkAssignment(webGoatUrlConfig.url("OpenRedirect/task2"), params, true)

        // Task 3: userinfo based host confusion
        params.clear()
        params["target"] = "https://webgoat.local@evil.com"
        params["token"] = "abc123"
        checkAssignment(webGoatUrlConfig.url("OpenRedirect/task3"), params, true)

        // Task 4: double-encoding bypass
        params.clear()
        params["target"] = "https://webgoat.local%2540evil.com"
        val task4Response =
            given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .post(webGoatUrlConfig.url("OpenRedirect/task4"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()

        assertThat(task4Response.getBoolean("lessonCompleted"), `is`(true))
        assertThat(task4Response.getString("output"), containsString("Double decode reveals external host"))
        assertThat(task4Response.getString("output"), containsString("2nd host: evil.com"))

        // Quiz completion with correct solutions
        val quizResponse =
            given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParam("question_0_solution", "Solution 0")
                .formParam("question_1_solution", "Solution 2")
                .formParam("question_2_solution", "Solution 0")
                .formParam("question_3_solution", "Solution 0")
                .post(webGoatUrlConfig.url("OpenRedirect/quiz"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()

        assertThat(quizResponse.getBoolean("lessonCompleted"), `is`(true))

        // Mitigation check requires external absolute URL
        params.clear()
        params["url"] = "https://attacker.integration"
        val mitigationResponse =
            given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParams(params)
                .post(webGoatUrlConfig.url("OpenRedirect/mitigation"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()

        assertThat(mitigationResponse.getBoolean("lessonCompleted"), `is`(true))
        assertThat(mitigationResponse.getString("output"), containsString("safe internal path"))

        checkResults("OpenRedirect")
    }
}
