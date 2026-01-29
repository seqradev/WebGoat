/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt.claimmisuse

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SigningKeyResolverAdapter
import io.jsonwebtoken.impl.TextCodec
import org.owasp.webgoat.container.LessonDataSource
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
import java.sql.SQLException

@RestController
@AssignmentHints(
    "jwt-kid-hint1",
    "jwt-kid-hint2",
    "jwt-kid-hint3",
    "jwt-kid-hint4",
    "jwt-kid-hint5",
    "jwt-kid-hint6",
)
@RequestMapping("/JWT/")
class JWTHeaderKIDEndpoint(
    private val dataSource: LessonDataSource,
) : AssignmentEndpoint {
    @PostMapping("kid/follow/{user}")
    @ResponseBody
    fun follow(
        @PathVariable("user") user: String,
    ): String =
        if (user == "Jerry") {
            "Following yourself seems redundant"
        } else {
            "You are now following Tom"
        }

    @PostMapping("kid/delete")
    @ResponseBody
    fun resetVotes(
        @RequestParam("token") token: String,
    ): AttackResult {
        if (token.isEmpty()) {
            return failed(this).feedback("jwt-invalid-token").build()
        } else {
            return try {
                var errorMessage: String? = null
                val jwt =
                    Jwts
                        .parser()
                        .setSigningKeyResolver(
                            object : SigningKeyResolverAdapter() {
                                override fun resolveSigningKeyBytes(
                                    header: JwsHeader<*>,
                                    claims: Claims,
                                ): ByteArray? {
                                    val kid = header["kid"] as? String
                                    try {
                                        dataSource.connection.use { connection ->
                                            val rs =
                                                connection
                                                    .createStatement()
                                                    .executeQuery("SELECT key FROM jwt_keys WHERE id = '$kid'")
                                            while (rs.next()) {
                                                return TextCodec.BASE64.decode(rs.getString(1))
                                            }
                                        }
                                    } catch (e: SQLException) {
                                        errorMessage = e.message
                                    }
                                    return null
                                }
                            },
                        ).parseClaimsJws(token)
                if (errorMessage != null) {
                    return failed(this).output(errorMessage).build()
                }
                val claims = jwt.body as Claims
                val username = claims["username"] as? String
                when (username) {
                    "Jerry" -> failed(this).feedback("jwt-final-jerry-account").build()
                    "Tom" -> success(this).build()
                    else -> failed(this).feedback("jwt-final-not-tom").build()
                }
            } catch (e: JwtException) {
                failed(this).feedback("jwt-invalid-token").output(e.toString()).build()
            }
        }
    }
}
