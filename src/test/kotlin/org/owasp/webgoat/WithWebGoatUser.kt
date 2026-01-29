/*
 * SPDX-FileCopyrightText: Copyright © 2024 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat

import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@WithSecurityContext(factory = WithMockWebGoatUserSecurityContextFactory::class)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithWebGoatUser(
    val username: String = "test",
    val password: String = "password",
)

class WithMockWebGoatUserSecurityContextFactory : WithSecurityContextFactory<WithWebGoatUser> {
    override fun createSecurityContext(customUser: WithWebGoatUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = WebGoatUser(customUser.username, customUser.password)
        val auth =
            UsernamePasswordAuthenticationToken.authenticated(
                principal,
                "password",
                principal.authorities,
            )
        context.authentication = auth
        return context
    }
}
