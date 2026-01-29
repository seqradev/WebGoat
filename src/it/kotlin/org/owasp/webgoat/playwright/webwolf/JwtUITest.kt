/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webwolf

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.RsaJsonWebKey
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.junit.jupiter.api.Test
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication

class JwtUITest : PlaywrightTest() {
    @Test
    fun shouldDecodeJwt(browser: Browser) {
        val page = Authentication.sylvester(browser)
        val secretKey = "test"
        val jwt =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" +
                ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        page.navigate(webWolfURL("jwt"))
        page.getByPlaceholder("Enter your secret key").fill(secretKey)
        page.getByPlaceholder("Paste token here").type(jwt)
        assertThat(page.locator("#header"))
            .hasValue("{\n  \"alg\" : \"HS256\",\n  \"typ\" : \"JWT\"\n}")
        assertThat(page.locator("#payload"))
            .hasValue(
                """
                {
                  "iat" : 1516239022,
                  "name" : "John Doe",
                  "sub" : "1234567890"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun shouldValidateJwtUsingJwks(browser: Browser) {
        val page = Authentication.sylvester(browser)

        val jwk: RsaJsonWebKey = RsaJwkGenerator.generateJwk(2048)
        jwk.keyId = "kid-1"
        val jws = JsonWebSignature()
        jws.payload = "{\"sub\":\"123\"}"
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        jws.key = jwk.privateKey
        jws.keyIdHeaderValue = jwk.keyId
        val rsaJwt = jws.compactSerialization
        val jwks = JsonWebKeySet(jwk).toJson(org.jose4j.jwk.JsonWebKey.OutputControlLevel.PUBLIC_ONLY)

        page.navigate(webWolfURL("jwt"))
        page.getByRole(AriaRole.RADIO, Page.GetByRoleOptions().setName("JWKS (public keys)")).check()
        page.getByPlaceholder("Paste token here").type(rsaJwt)
        page.locator("#jwks").fill(jwks)
        assertThat(page.locator("#signatureValid")).hasText("Signature valid")
    }
}
