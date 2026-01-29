/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.hijacksession.cas

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HijackSessionAuthenticationProviderTest {
    private val provider = HijackSessionAuthenticationProvider()

    @ParameterizedTest
    @DisplayName("Provider authentication test")
    @MethodSource("authenticationForCookieValues")
    fun testProviderAuthenticationGeneratesCookie(authentication: Authentication?) {
        val auth = provider.authenticate(authentication)
        assertThat(auth.id, not(auth.id.isNullOrEmpty()))
    }

    @Test
    fun testAuthenticated() {
        val id = "anyId"
        provider.addSession(id)

        var auth = provider.authenticate(Authentication.builder().id(id).build())

        assertThat(auth.id, `is`(id))
        assertThat(auth.isAuthenticated, `is`(true))

        auth = provider.authenticate(Authentication.builder().id("otherId").build())

        assertThat(auth.id, `is`("otherId"))
        assertThat(auth.isAuthenticated, `is`(false))
    }

    @Test
    fun testAuthenticationToString() {
        val authBuilder =
            Authentication
                .builder()
                .name("expectedName")
                .credentials("expectedCredentials")
                .id("expectedId")

        val auth = authBuilder.build()

        var expected =
            "Authentication.AuthenticationBuilder(" +
                "name=${auth.name}, credentials=${auth.credentials}, id=${auth.id})"

        assertThat(authBuilder.toString(), `is`(expected))

        expected =
            "Authentication(authenticated=${auth.isAuthenticated}" +
            ", name=${auth.name}, credentials=${auth.credentials}, id=${auth.id})"

        assertThat(auth.toString(), `is`(expected))
    }

    @Test
    fun testMaxSessions() {
        for (i in 0..HijackSessionAuthenticationProvider.MAX_SESSIONS + 1) {
            provider.authorizedUserAutoLogin()
            provider.addSession(null)
        }

        assertThat(provider.getSessionsSize(), `is`(HijackSessionAuthenticationProvider.MAX_SESSIONS))
    }

    companion object {
        @JvmStatic
        fun authenticationForCookieValues(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null as Authentication?),
                Arguments.of(
                    Authentication
                        .builder()
                        .name("any")
                        .credentials("any")
                        .build(),
                ),
                Arguments.of(Authentication.builder().id("any").build()),
            )
    }
}
