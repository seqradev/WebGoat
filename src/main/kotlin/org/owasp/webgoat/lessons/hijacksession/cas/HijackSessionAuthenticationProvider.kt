/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.hijacksession.cas

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.ApplicationScope
import java.time.Instant
import java.util.LinkedList
import java.util.Queue
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import java.util.function.DoublePredicate
import java.util.function.Supplier

// weak id value and mechanism

@ApplicationScope
@Component
class HijackSessionAuthenticationProvider : AuthenticationProvider<Authentication> {
    private val sessions: Queue<String> = LinkedList()

    override fun authenticate(authentication: Authentication?): Authentication {
        if (authentication == null) {
            return AUTHENTICATION_SUPPLIER.get()
        }

        if (!authentication.id.isNullOrEmpty() && sessions.contains(authentication.id)) {
            authentication.isAuthenticated = true
            return authentication
        }

        if (authentication.id.isNullOrEmpty()) {
            authentication.id = GENERATE_SESSION_ID.get()
        }

        authorizedUserAutoLogin()

        return authentication
    }

    fun authorizedUserAutoLogin() {
        if (!PROBABILITY_DOUBLE_PREDICATE.test(ThreadLocalRandom.current().nextDouble())) {
            val authentication = AUTHENTICATION_SUPPLIER.get()
            authentication.isAuthenticated = true
            addSession(authentication.id)
        }
    }

    fun addSession(sessionId: String?): Boolean {
        if (sessions.size >= MAX_SESSIONS) {
            sessions.remove()
        }
        return sessions.add(sessionId)
    }

    fun getSessionsSize(): Int = sessions.size

    companion object {
        private var id = Random().nextLong() and Long.MAX_VALUE

        const val MAX_SESSIONS = 50

        private val PROBABILITY_DOUBLE_PREDICATE = DoublePredicate { pr -> pr < 0.75 }
        private val GENERATE_SESSION_ID =
            Supplier { "${++id}-${Instant.now().toEpochMilli()}" }

        @JvmField
        val AUTHENTICATION_SUPPLIER =
            Supplier { Authentication.builder().id(GENERATE_SESSION_ID.get()).build() }
    }
}
