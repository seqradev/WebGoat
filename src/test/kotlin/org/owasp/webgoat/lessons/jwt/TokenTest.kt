/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SigningKeyResolverAdapter
import io.jsonwebtoken.impl.TextCodec
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

class TokenTest {
    private val log = LoggerFactory.getLogger(TokenTest::class.java)

    @Test
    fun test() {
        val key = "qwertyqwerty1234"
        val claims =
            mapOf(
                "username" to "Jerry",
                "aud" to "webgoat.org",
                "email" to "jerry@webgoat.com",
            )
        val token =
            Jwts
                .builder()
                .setHeaderParam("kid", "webgoat_key")
                .setIssuedAt(Date(System.currentTimeMillis() + TimeUnit.DAYS.toDays(10)))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact()
        log.debug(token)
        Jwts.parser().setSigningKey("qwertyqwerty1234").parse(token)
        Jwts
            .parser()
            .setSigningKeyResolver(
                object : SigningKeyResolverAdapter() {
                    override fun resolveSigningKeyBytes(
                        header: JwsHeader<*>,
                        claims: Claims,
                    ): ByteArray = TextCodec.BASE64.decode(key)
                },
            ).parse(token)
    }

    @Test
    fun testRefresh() {
        val now = Instant.now()
        val claims = Jwts.claims().setIssuedAt(Date.from(now.minus(Duration.ofDays(10))))
        claims.expiration = Date.from(now.minus(Duration.ofDays(9)))
        claims["admin"] = "false"
        claims["user"] = "Tom"
        val token =
            Jwts
                .builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, "bm5n3SkxCX4kKRy4")
                .compact()
        log.debug(token)
    }
}
