/*
 * SPDX-FileCopyrightText: Copyright © 2023 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt.claimmisuse

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm.RS256
import org.hamcrest.Matchers.`is`
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJsonWebKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

class JWTHeaderJKUEndpointTest : LessonTest() {
    private lateinit var keyPair: KeyPair
    private lateinit var webwolfServer: WireMockServer
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

        setupWebWolf()
        keyPair = generateRsaKey()
    }

    private fun setupWebWolf() {
        webwolfServer = WireMockServer(options().dynamicPort())
        webwolfServer.start()
        port = webwolfServer.port()
    }

    private fun generateRsaKey(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    @Test
    fun solve() {
        setupJsonWebKeySetInWebWolf()
        val token = createTokenAndSignIt()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/jku/delete").param("token", token).content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    @DisplayName("When JWKS is not present in WebWolf then the call should fail")
    fun shouldFailNotPresent() {
        val token = createTokenAndSignIt()

        mockMvc
            .perform(MockMvcRequestBuilders.post("/JWT/jku/delete").param("token", token).content(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    private fun createTokenAndSignIt(): String =
        Jwts
            .builder()
            .setHeaderParam("jku", "http://localhost:$port/files/jwks")
            .setClaims(mapOf("username" to "Tom"))
            .signWith(RS256, keyPair.private)
            .compact()

    private fun setupJsonWebKeySetInWebWolf() {
        val jwks = JsonWebKeySet(RsaJsonWebKey(keyPair.public as RSAPublicKey))
        webwolfServer.stubFor(
            WireMock
                .get(WireMock.urlMatching("/files/jwks"))
                .willReturn(aResponse().withStatus(200).withBody(jwks.toJson())),
        )
    }
}
