/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import org.springframework.security.core.token.Sha512DigestUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PathTraversalIT : IntegrationTest() {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var fileToUpload: File

    @BeforeEach
    fun init() {
        fileToUpload = Files.createFile(tempDir.resolve("test.jpg")).toFile()
        Files.write(fileToUpload.toPath(), "This is a test".toByteArray())
        startLesson("PathTraversal")
    }

    @TestFactory
    fun testPathTraversal(): Iterable<DynamicTest> =
        listOf(
            dynamicTest("assignment 1 - profile upload") { assignment1() },
            dynamicTest("assignment 2 - profile upload fix") { assignment2() },
            dynamicTest("assignment 3 - profile upload remove user input") { assignment3() },
            dynamicTest("assignment 4 - profile upload random pic") { assignment4() },
            dynamicTest("assignment 5 - zip slip") { assignment5() },
        )

    private fun assignment1() {
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .multiPart("uploadedFile", "test.jpg", Files.readAllBytes(fileToUpload.toPath()))
                .param("fullName", "../John Doe")
                .post(webGoatUrlConfig.url("PathTraversal/profile-upload"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )
    }

    private fun assignment2() {
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .multiPart("uploadedFileFix", "test.jpg", Files.readAllBytes(fileToUpload.toPath()))
                .param("fullNameFix", "..././John Doe")
                .post(webGoatUrlConfig.url("PathTraversal/profile-upload-fix"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )
    }

    private fun assignment3() {
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .multiPart(
                    "uploadedFileRemoveUserInput",
                    "../test.jpg",
                    Files.readAllBytes(fileToUpload.toPath()),
                ).post(webGoatUrlConfig.url("PathTraversal/profile-upload-remove-user-input"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )
    }

    private fun assignment4() {
        val uri = "PathTraversal/random-picture?id=%2E%2E%2F%2E%2E%2Fpath-traversal-secret"
        RestAssured
            .given()
            .urlEncodingEnabled(false)
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .get(webGoatUrlConfig.url(uri))
            .then()
            .statusCode(200)
            .body(`is`("You found it submit the SHA-512 hash of your username as answer"))

        checkAssignment(
            webGoatUrlConfig.url("PathTraversal/random"),
            mapOf("secret" to Sha512DigestUtils.shaHex(user)),
            true,
        )
    }

    private fun assignment5() {
        var webGoatHome = webGoatServerDirectory() + "PathTraversal/" + user
        webGoatHome = webGoatHome.replace(Regex("^[a-zA-Z]:"), "") // Remove C: from the home directory on Windows

        val webGoatDirectory = File(webGoatHome)
        val zipFile = File(tempDir.toFile(), "upload.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val e = ZipEntry("../../../../../../../../../../$webGoatDirectory/image.jpg")
            zos.putNextEntry(e)
            zos.write("test".toByteArray(StandardCharsets.UTF_8))
        }
        assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .multiPart("uploadedFileZipSlip", "upload.zip", Files.readAllBytes(zipFile.toPath()))
                .post(webGoatUrlConfig.url("PathTraversal/zip-slip"))
                .then()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            `is`(true),
        )
    }

    @AfterEach
    fun shutdown() {
        // this will run only once after the list of dynamic tests has run, this is to test if the
        // lesson is marked complete
        checkResults("PathTraversal")
    }
}
