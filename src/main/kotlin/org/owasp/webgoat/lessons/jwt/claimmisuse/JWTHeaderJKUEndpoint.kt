/*
 * SPDX-FileCopyrightText: Copyright © 2023 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt.claimmisuse

import com.auth0.jwk.JwkException
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.net.MalformedURLException
import java.net.URL
import java.security.interfaces.RSAPublicKey

@RequestMapping("/JWT/")
@RestController
@AssignmentHints("jwt-jku-hint1", "jwt-jku-hint2", "jwt-jku-hint3", "jwt-jku-hint4", "jwt-jku-hint5")
class JWTHeaderJKUEndpoint : AssignmentEndpoint {
    @PostMapping("jku/follow/{user}")
    @ResponseBody
    fun follow(
        @PathVariable("user") user: String,
    ): String =
        if (user == "Jerry") {
            "Following yourself seems redundant"
        } else {
            "You are now following Tom"
        }

    @PostMapping("jku/delete")
    @ResponseBody
    fun resetVotes(
        @RequestParam("token") token: String,
    ): AttackResult {
        if (token.isEmpty()) {
            return failed(this).feedback("jwt-invalid-token").build()
        } else {
            return try {
                val decodedJWT = JWT.decode(token)
                val jku = decodedJWT.getHeaderClaim("jku")
                val jwkProvider = JwkProviderBuilder(URL(jku.asString())).build()
                val jwk = jwkProvider.get(decodedJWT.keyId)
                val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
                JWT.require(algorithm).build().verify(decodedJWT)

                val username = decodedJWT.claims["username"]?.asString()
                when (username) {
                    "Jerry" -> failed(this).feedback("jwt-final-jerry-account").build()
                    "Tom" -> success(this).build()
                    else -> failed(this).feedback("jwt-final-not-tom").build()
                }
            } catch (e: MalformedURLException) {
                failed(this).feedback("jwt-invalid-token").output(e.toString()).build()
            } catch (e: JWTVerificationException) {
                failed(this).feedback("jwt-invalid-token").output(e.toString()).build()
            } catch (e: JwkException) {
                failed(this).feedback("jwt-invalid-token").output(e.toString()).build()
            }
        }
    }
}
