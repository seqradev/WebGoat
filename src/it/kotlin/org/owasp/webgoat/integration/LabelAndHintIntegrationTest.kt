/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.util.Properties

class LabelAndHintIntegrationTest : IntegrationTest() {
    companion object {
        const val ESCAPE_JSON_PATH_CHAR = "'"
    }

    @Test
    fun testSingleLabel() {
        assertTrue(true)
        var jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/labels.mvc"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()

        assertEquals(
            "Try again: but this time enter a value before hitting go.",
            jsonPath.getString("${ESCAPE_JSON_PATH_CHAR}http-basics.close${ESCAPE_JSON_PATH_CHAR}"),
        )

        // check if lang parameter overrules Accept-Language parameter
        jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/labels.mvc?lang=nl"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
        assertEquals(
            "Gebruikersnaam",
            jsonPath.getString("${ESCAPE_JSON_PATH_CHAR}username${ESCAPE_JSON_PATH_CHAR}"),
        )

        jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/labels.mvc?lang=de"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
        assertEquals(
            "Benutzername",
            jsonPath.getString("${ESCAPE_JSON_PATH_CHAR}username${ESCAPE_JSON_PATH_CHAR}"),
        )

        // check if invalid language returns english
        jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nl")
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/labels.mvc?lang=xx"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
        assertEquals(
            "Username",
            jsonPath.getString("${ESCAPE_JSON_PATH_CHAR}username${ESCAPE_JSON_PATH_CHAR}"),
        )

        // check if invalid language returns english
        jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "xx_YY")
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/labels.mvc"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
        assertEquals(
            "Username",
            jsonPath.getString("${ESCAPE_JSON_PATH_CHAR}username${ESCAPE_JSON_PATH_CHAR}"),
        )
    }

    @Test
    fun testHints() {
        val jsonPathLabels = getLabels("en")
        val allLessons =
            listOf(
                "HttpBasics",
                "HttpProxies",
                "CIA",
                "InsecureLogin",
                "Cryptography",
                "PathTraversal",
                "XXE",
                "JWT",
                "IDOR",
                "SSRF",
                "WebWolfIntroduction",
                "CrossSiteScripting",
                "CSRF",
                "HijackSession",
                "SqlInjection",
                "SqlInjectionMitigations",
                "SqlInjectionAdvanced",
                "Challenge1",
            )
        for (lesson in allLessons) {
            startLesson(lesson)
            val hintKeys = getHints()
            for (key in hintKeys) {
                val keyValue = jsonPathLabels.getString("$ESCAPE_JSON_PATH_CHAR$key$ESCAPE_JSON_PATH_CHAR")
                assertNotNull(keyValue)
                assertNotEquals(key, keyValue)
            }
        }
    }

    @Test
    fun testLabels() {
        val jsonPathLabels = getLabels("en")
        val propsDefault = getProperties("")
        for (key in propsDefault.stringPropertyNames()) {
            val keyValue = jsonPathLabels.getString("$ESCAPE_JSON_PATH_CHAR$key$ESCAPE_JSON_PATH_CHAR")
            assertNotNull(keyValue)
        }
        checkLang(propsDefault, "nl")
        checkLang(propsDefault, "de")
        checkLang(propsDefault, "fr")
    }

    private fun getProperties(lang: String): Properties {
        val langSuffix = if (lang.isEmpty()) "" else "_$lang"
        return Properties().also { prop ->
            try {
                FileInputStream("src/main/resources/i18n/messages$langSuffix.properties").use { input ->
                    prop.load(input)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkLang(
        propsDefault: Properties,
        lang: String,
    ) {
        val jsonPath = getLabels(lang)
        val propsLang = getProperties(lang)

        for (key in propsLang.stringPropertyNames()) {
            if (!propsDefault.containsKey(key)) {
                System.err.println("key: $key in ($lang) is missing from default properties")
                fail<Unit>()
            }
            if (jsonPath.getString("$ESCAPE_JSON_PATH_CHAR$key$ESCAPE_JSON_PATH_CHAR") != propsLang.getProperty(key)) {
                println("key: $key in ($lang) has incorrect translation in label service")
                println("actual:" + jsonPath.getString("$ESCAPE_JSON_PATH_CHAR$key$ESCAPE_JSON_PATH_CHAR"))
                println("expected: " + propsLang.getProperty(key))
                println()
                fail<Unit>()
            }
        }
    }

    private fun getLabels(lang: String): JsonPath =
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .contentType(ContentType.JSON)
            .header("Accept-Language", lang)
            .cookie("JSESSIONID", webGoatCookie)
            .get(webGoatUrlConfig.url("service/labels.mvc"))
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

    private fun getHints(): List<String> {
        val jsonPath =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("service/hint.mvc"))
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
        return jsonPath.getList("hint")
    }
}
