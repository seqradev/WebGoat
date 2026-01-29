/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ProgressRaceConditionIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        val numberOfCalls = 40
        val numberOfParallelThreads = 5
        startLesson("Challenge1")

        val call =
            Callable<Response> {
                RestAssured
                    .given()
                    .`when`()
                    .relaxedHTTPSValidation()
                    .cookie("JSESSIONID", webGoatCookie)
                    .formParams(mapOf("flag" to "test"))
                    .post(webGoatUrlConfig.url("challenge/flag/1"))
            }
        val executorService = Executors.newFixedThreadPool(numberOfParallelThreads)
        val flagCalls = (0 until numberOfCalls).map { call }
        val responses = executorService.invokeAll(flagCalls)

        // A certain amount of parallel calls should fail as optimistic locking in DB is applied
        val countStatusCode500 =
            responses.count { r ->
                try {
                    r.get().statusCode != 200
                } catch (e: Exception) {
                    throw IllegalStateException(e)
                }
            }
        System.err.println("counted status 500: $countStatusCode500")
        assertThat(countStatusCode500)
            .isLessThanOrEqualTo(numberOfCalls - (numberOfCalls / numberOfParallelThreads))
    }
}
