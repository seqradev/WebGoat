/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.jwt

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.jwx.CompactSerializer
import org.jose4j.keys.HmacKey
import org.jose4j.lang.JoseException
import org.jose4j.lang.UnresolvableKeyException
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.TreeMap

data class JWTToken(
    val encoded: String? = null,
    val secretKey: String? = null,
    val header: String? = null,
    val validHeader: Boolean = false,
    val validPayload: Boolean = false,
    val validToken: Boolean = false,
    val payload: String? = null,
    val signatureValid: Boolean = true,
) {
    companion object {
        @JvmStatic
        fun decode(
            jwt: String,
            secretKey: String?,
        ): JWTToken = decode(jwt, secretKey, null)

        @JvmStatic
        fun decode(
            jwt: String,
            secretKey: String?,
            jwksJson: String?,
        ): JWTToken {
            val cleanedToken = jwt.trim().replace(System.getProperty("line.separator"), "")
            val token = parseToken(cleanedToken)
            return token.copy(signatureValid = validateSignature(secretKey, jwksJson, cleanedToken))
        }

        @JvmStatic
        fun encode(
            header: String?,
            payloadAsString: String?,
            secretKey: String?,
        ): JWTToken {
            val headers = parse(header ?: "")
            val payload = parse(payloadAsString ?: "")

            var token =
                JWTToken(
                    header = write(header ?: "", headers),
                    payload = write(payloadAsString ?: "", payload),
                    validHeader = header.isNullOrBlank() || headers.isNotEmpty(),
                    validToken = true,
                    validPayload = payloadAsString.isNullOrBlank() || payload.isNotEmpty(),
                )

            val jws = JsonWebSignature()
            jws.payload = payloadAsString
            headers.forEach { (k, v) -> jws.setHeader(k, v) }
            if (headers.isNotEmpty()) { // otherwise e30 meaning {} will be shown as header
                token =
                    token.copy(
                        encoded =
                            CompactSerializer.serialize(
                                jws.headers.encodedHeader,
                                jws.encodedPayload,
                            ),
                    )
            }

            // Only sign when valid header and payload
            secretKey?.takeIf { headers.isNotEmpty() && payload.isNotEmpty() && it.isNotBlank() }?.let { key ->
                jws.setDoKeyValidation(false)
                jws.key = HmacKey(key.toByteArray(StandardCharsets.UTF_8))
                try {
                    token = token.copy(encoded = jws.compactSerialization, signatureValid = true)
                } catch (e: JoseException) {
                    // Do nothing
                }
            }
            return token
        }

        private fun parse(header: String): Map<String, Any> {
            val reader = ObjectMapper()
            return try {
                reader.readValue(header, TreeMap::class.java) as Map<String, Any>
            } catch (e: JsonProcessingException) {
                emptyMap()
            }
        }

        private fun write(
            originalValue: String,
            data: Map<String, Any>,
        ): String {
            val writer = ObjectMapper().writerWithDefaultPrettyPrinter()
            return try {
                if (data.isEmpty()) {
                    originalValue
                } else {
                    writer.writeValueAsString(data)
                }
            } catch (e: JsonProcessingException) {
                originalValue
            }
        }

        private fun parseToken(jwt: String): JWTToken {
            val token = jwt.split("\\.".toRegex())

            if (token.size >= 2) {
                val header =
                    String(Base64.getUrlDecoder().decode(token[0]), StandardCharsets.UTF_8)
                val payloadAsString =
                    String(Base64.getUrlDecoder().decode(token[1]), StandardCharsets.UTF_8)
                val headers = parse(header)
                val payload = parse(payloadAsString)
                return JWTToken(
                    encoded = jwt,
                    header = write(header, headers),
                    payload = write(payloadAsString, payload),
                    validHeader = headers.isNotEmpty(),
                    validPayload = payload.isNotEmpty(),
                    validToken = headers.isNotEmpty() && payload.isNotEmpty(),
                )
            }
            return JWTToken(encoded = jwt, validToken = false)
        }

        private fun validateSignature(
            secretKey: String?,
            jwksJson: String?,
            jwt: String,
        ): Boolean =
            secretKey?.takeIf { it.isNotBlank() }?.let { validateWithSharedSecret(it, jwt) }
                ?: jwksJson?.takeIf { it.isNotBlank() }?.let { validateWithJwks(it, jwt) }
                ?: false

        private fun validateWithSharedSecret(
            secretKey: String,
            jwt: String,
        ): Boolean {
            val jwtConsumer =
                JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setVerificationKey(HmacKey(secretKey.toByteArray(StandardCharsets.UTF_8)))
                    .setRelaxVerificationKeyValidation()
                    .build()
            return try {
                jwtConsumer.processToClaims(jwt)
                true
            } catch (e: InvalidJwtException) {
                false
            }
        }

        private fun validateWithJwks(
            jwksJson: String,
            jwt: String,
        ): Boolean =
            try {
                val jsonWebKeySet = JsonWebKeySet(jwksJson)
                val resolver =
                    org.jose4j.keys.resolvers.VerificationKeyResolver { jws, _ ->
                        val keyId = jws.keyIdHeaderValue
                        keyId?.takeIf { it.isNotBlank() }?.let { id ->
                            jsonWebKeySet.jsonWebKeys.firstOrNull { it.keyId == id }?.key
                        } ?: jsonWebKeySet.jsonWebKeys.firstOrNull()?.key
                            ?: throw UnresolvableKeyException("No keys available in JWKS")
                    }

                val jwtConsumer =
                    JwtConsumerBuilder()
                        .setSkipAllValidators()
                        .setVerificationKeyResolver(resolver)
                        .setRelaxVerificationKeyValidation()
                        .build()
                jwtConsumer.processToClaims(jwt)
                true
            } catch (e: JoseException) {
                false
            } catch (e: InvalidJwtException) {
                false
            }
    }
}
