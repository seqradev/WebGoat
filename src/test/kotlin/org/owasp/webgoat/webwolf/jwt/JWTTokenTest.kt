/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.jose4j.jwk.JsonWebKey.OutputControlLevel
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.junit.jupiter.api.Test

class JWTTokenTest {
    @Test
    fun encodeCorrectTokenWithoutSignature() {
        val headers = mapOf("alg" to "HS256", "typ" to "JWT")
        val payload = mapOf("test" to "test")
        val token = JWTToken.encode(toString(headers), toString(payload), "")

        assertThat(token.encoded)
            .isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0IjoidGVzdCJ9")
    }

    @Test
    fun encodeCorrectTokenWithSignature() {
        val headers = mapOf("alg" to "HS256", "typ" to "JWT")
        val payload = mapOf("test" to "test")
        val token = JWTToken.encode(toString(headers), toString(payload), "webgoat")

        assertThat(token.encoded)
            .isEqualTo(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0IjoidGVzdCJ9.axNp9BkswwK_YRF2URJ5P1UejQNYZbK4qYcMnkusg6I",
            )
    }

    @Test
    fun encodeTokenWithNonJsonInput() {
        val token = JWTToken.encode("aaa", "bbb", "test")

        assertThat(token.encoded).isNullOrEmpty()
    }

    @Test
    fun decodeValidSignedToken() {
        val token =
            JWTToken.decode(
                "eyJhbGciOiJIUzI1NiJ9.eyJ0ZXN0IjoidGVzdCJ9.KOobRHDYyaesV_doOk11XXGKSONwzllraAaqqM4VFE4",
                "test",
            )

        assertThat(token.header).contains("\"alg\" : \"HS256\"")
        assertThat(token.signatureValid).isTrue()
    }

    @Test
    fun decodeInvalidSignedToken() {
        val token =
            JWTToken.decode(
                "eyJhbGciOiJIUzI1NiJ9.eyJ0ZXsdfdfsaasfddfasN0IjoidGVzdCJ9.KOobRHDYyaesV_doOk11XXGKSONwzllraAaqqM4VFE4",
                "",
            )

        assertThat(token.header).contains("\"alg\" : \"HS256\"")
        assertThat(token.payload).contains("{\"te")
    }

    @Test
    fun onlyEncodeWhenHeaderOrPayloadIsPresent() {
        val token = JWTToken.encode("", "", "")

        assertThat(token.encoded).isNullOrEmpty()
    }

    @Test
    fun encodeAlgNone() {
        val headers = mapOf("alg" to "none")
        val payload = mapOf("test" to "test")
        val token = JWTToken.encode(toString(headers), toString(payload), "test")

        assertThat(token.encoded).isEqualTo("eyJhbGciOiJub25lIn0.eyJ0ZXN0IjoidGVzdCJ9")
    }

    @Test
    fun decodeValidRsaSignedTokenUsingJwks() {
        val jwk = RsaJwkGenerator.generateJwk(2048)
        jwk.keyId = "kid-1"

        val jws = JsonWebSignature()
        jws.payload = toString(mapOf("role" to "admin"))
        jws.key = jwk.privateKey
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        jws.keyIdHeaderValue = jwk.keyId
        val compact = jws.compactSerialization

        val jwksJson = JsonWebKeySet(jwk).toJson(OutputControlLevel.PUBLIC_ONLY)

        val token = JWTToken.decode(compact, null, jwksJson)

        assertThat(token.signatureValid).isTrue()
        assertThat(token.header).contains("\"kid\" : \"kid-1\"")
    }

    @Test
    fun decodeTokenFailsWithMismatchingJwks() {
        val signer = RsaJwkGenerator.generateJwk(2048)
        signer.keyId = "signing-key"
        val jws = JsonWebSignature()
        jws.payload = toString(mapOf("scope" to "read"))
        jws.key = signer.privateKey
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        jws.keyIdHeaderValue = signer.keyId
        val compact = jws.compactSerialization

        val otherKey = RsaJwkGenerator.generateJwk(2048)
        otherKey.keyId = "other-key"
        val wrongJwks = JsonWebKeySet(otherKey).toJson(OutputControlLevel.PUBLIC_ONLY)

        val token = JWTToken.decode(compact, null, wrongJwks)

        assertThat(token.signatureValid).isFalse()
    }

    private fun toString(map: Map<String, String>): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(map)
    }
}
