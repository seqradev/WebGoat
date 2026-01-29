/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.TextCodec
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.jwt.votes.Views
import org.owasp.webgoat.lessons.jwt.votes.Vote
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant
import java.util.Date

@RestController
@AssignmentHints(
    "jwt-change-token-hint1",
    "jwt-change-token-hint2",
    "jwt-change-token-hint3",
    "jwt-change-token-hint4",
    "jwt-change-token-hint5",
)
class JWTVotesEndpoint : AssignmentEndpoint {
    private val votes = mutableMapOf<String, Vote>()

    @PostConstruct
    fun initVotes() {
        votes["Admin lost password"] =
            Vote(
                title = "Admin lost password",
                information =
                    "In this challenge you will need to help the admin and find the password in order to login",
                imageSmall = "challenge1-small.png",
                imageBig = "challenge1.png",
                numberOfVotes = 36000,
                totalVotes = TOTAL_VOTES,
            )
        votes["Vote for your favourite"] =
            Vote(
                title = "Vote for your favourite",
                information = "In this challenge ...",
                imageSmall = "challenge5-small.png",
                imageBig = "challenge5.png",
                numberOfVotes = 30000,
                totalVotes = TOTAL_VOTES,
            )
        votes["Get it for free"] =
            Vote(
                title = "Get it for free",
                information = "The objective for this challenge is to buy a Samsung phone for free.",
                imageSmall = "challenge2-small.png",
                imageBig = "challenge2.png",
                numberOfVotes = 20000,
                totalVotes = TOTAL_VOTES,
            )
        votes["Photo comments"] =
            Vote(
                title = "Photo comments",
                information = "n this challenge you can comment on the photo you will need to find the flag somewhere.",
                imageSmall = "challenge3-small.png",
                imageBig = "challenge3.png",
                numberOfVotes = 10000,
                totalVotes = TOTAL_VOTES,
            )
    }

    @GetMapping("/JWT/votings/login")
    fun login(
        @RequestParam("user") user: String,
        response: HttpServletResponse,
    ) {
        if (VALID_USERS.contains(user)) {
            val claims = Jwts.claims().setIssuedAt(Date.from(Instant.now().plus(Duration.ofDays(10))))
            claims["admin"] = "false"
            claims["user"] = user
            val token =
                Jwts
                    .builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS512, JWT_PASSWORD)
                    .compact()
            val cookie = Cookie("access_token", token)
            response.addCookie(cookie)
            response.status = HttpStatus.OK.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
        } else {
            val cookie = Cookie("access_token", "")
            response.addCookie(cookie)
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
        }
    }

    @GetMapping("/JWT/votings")
    @ResponseBody
    fun getVotes(
        @CookieValue(value = "access_token", required = false) accessToken: String?,
    ): MappingJacksonValue {
        val value =
            MappingJacksonValue(
                votes.values.sortedByDescending { it.average }.toList(),
            )
        if (accessToken.isNullOrEmpty()) {
            value.serializationView = Views.GuestView::class.java
        } else {
            try {
                val jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken)
                val claims = jwt.body as Claims
                val user = claims["user"] as? String
                if (user == "Guest" || user == null || !VALID_USERS.contains(user)) {
                    value.serializationView = Views.GuestView::class.java
                } else {
                    value.serializationView = Views.UserView::class.java
                }
            } catch (e: JwtException) {
                value.serializationView = Views.GuestView::class.java
            }
        }
        return value
    }

    @PostMapping(value = ["/JWT/votings/{title}"])
    @ResponseBody
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun vote(
        @PathVariable title: String,
        @CookieValue(value = "access_token", required = false) accessToken: String?,
    ): ResponseEntity<Unit> {
        if (accessToken.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } else {
            return try {
                val jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken)
                val claims = jwt.body as Claims
                val user = claims["user"] as? String
                if (user == null || !VALID_USERS.contains(user)) {
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                } else {
                    votes[title]?.incrementNumberOfVotes(TOTAL_VOTES)
                    ResponseEntity.accepted().build()
                }
            } catch (e: JwtException) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }
        }
    }

    @PostMapping("/JWT/votings")
    @ResponseBody
    fun resetVotes(
        @CookieValue(value = "access_token", required = false) accessToken: String?,
    ): AttackResult {
        if (accessToken.isNullOrEmpty()) {
            return failed(this).feedback("jwt-invalid-token").build()
        } else {
            return try {
                val jwt = Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken)
                val claims = jwt.body as Claims
                val isAdmin = claims["admin"]?.toString().toBoolean()
                if (!isAdmin) {
                    failed(this).feedback("jwt-only-admin").build()
                } else {
                    votes.values.forEach { it.reset() }
                    success(this).build()
                }
            } catch (e: JwtException) {
                failed(this).feedback("jwt-invalid-token").output(e.toString()).build()
            }
        }
    }

    companion object {
        @JvmField
        val JWT_PASSWORD: String = TextCodec.BASE64.encode("victory")

        private const val VALID_USERS = "TomJerrySylvester"
        private const val TOTAL_VOTES = 38929
    }
}
