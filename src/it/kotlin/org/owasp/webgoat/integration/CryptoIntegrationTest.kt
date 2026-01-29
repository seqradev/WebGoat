/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.owasp.webgoat.lessons.cryptography.CryptoUtil
import org.owasp.webgoat.lessons.cryptography.HashingAssignment
import java.nio.charset.Charset
import java.security.interfaces.RSAPrivateKey
import java.util.Base64
import javax.xml.bind.DatatypeConverter

class CryptoIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        startLesson("Cryptography")

        checkAssignment2()
        checkAssignment3()

        // Assignment 4
        try {
            checkAssignment4()
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }

        try {
            checkAssignmentSigning()
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }

        checkAssignmentDefaults()

        checkResults("Cryptography")
    }

    private fun checkAssignment2() {
        var basicEncoding =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("crypto/encoding/basic"))
                .then()
                .extract()
                .asString()
        basicEncoding = basicEncoding.substring("Authorization: Basic ".length)
        val decodedString = String(Base64.getDecoder().decode(basicEncoding.toByteArray()))
        val answerUser = decodedString.split(":")[0]
        val answerPwd = decodedString.split(":")[1]
        val params = mutableMapOf<String, Any>()
        params["answer_user"] = answerUser
        params["answer_pwd"] = answerPwd
        checkAssignment(webGoatUrlConfig.url("crypto/encoding/basic-auth"), params, true)
    }

    private fun checkAssignment3() {
        val answer1 = "databasepassword"
        val params = mutableMapOf<String, Any>()
        params["answer_pwd1"] = answer1
        checkAssignment(webGoatUrlConfig.url("crypto/encoding/xor"), params, true)
    }

    private fun checkAssignment4() {
        val md5Hash =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("crypto/hashing/md5"))
                .then()
                .extract()
                .asString()

        val sha256Hash =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("crypto/hashing/sha256"))
                .then()
                .extract()
                .asString()

        var answer1 = "unknown"
        var answer2 = "unknown"
        for (secret in HashingAssignment.SECRETS) {
            if (md5Hash == HashingAssignment.getHash(secret, "MD5")) {
                answer1 = secret
            }
            if (sha256Hash == HashingAssignment.getHash(secret, "SHA-256")) {
                answer2 = secret
            }
        }

        val params = mutableMapOf<String, Any>()
        params["answer_pwd1"] = answer1
        params["answer_pwd2"] = answer2
        checkAssignment(webGoatUrlConfig.url("crypto/hashing"), params, true)
    }

    private fun checkAssignmentSigning() {
        val privatePEM =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("crypto/signing/getprivate"))
                .then()
                .extract()
                .asString()
        val privateKey = CryptoUtil.getPrivateKeyFromPEM(privatePEM)

        val privk = privateKey as RSAPrivateKey
        val modulus = DatatypeConverter.printHexBinary(privk.modulus.toByteArray())
        val signature = CryptoUtil.signMessage(modulus, privateKey) ?: ""
        val params = mutableMapOf<String, Any>()
        params["modulus"] = modulus
        params["signature"] = signature
        checkAssignment(webGoatUrlConfig.url("crypto/signing/verify"), params, true)
    }

    private fun checkAssignmentDefaults() {
        val text =
            String(
                Base64.getDecoder().decode(
                    "TGVhdmluZyBwYXNzd29yZHMgaW4gZG9ja2VyIGltYWdlcyBpcyBub3Qgc28gc2VjdXJl"
                        .toByteArray(Charset.forName("UTF-8")),
                ),
            )

        val params = mutableMapOf<String, Any>()
        params["secretText"] = text
        params["secretFileName"] = "default_secret"
        checkAssignment(webGoatUrlConfig.url("crypto/secure/defaults"), params, true)
    }
}
