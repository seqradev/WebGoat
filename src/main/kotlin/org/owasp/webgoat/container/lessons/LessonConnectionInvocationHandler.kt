/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.security.core.context.SecurityContextHolder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.sql.Connection

/**
 * Handler which sets the correct schema for the currently bounded user. This way users are not
 * seeing each other data, and we can reset data for just one particular user.
 */
class LessonConnectionInvocationHandler(
    private val targetConnection: Connection,
) : InvocationHandler {
    @Throws(Throwable::class)
    override fun invoke(
        proxy: Any,
        method: Method,
        args: Array<Any?>?,
    ): Any? {
        (SecurityContextHolder.getContext().authentication?.principal as? WebGoatUser)?.let { user ->
            targetConnection.createStatement().use { statement ->
                statement.execute("SET SCHEMA \"${user.username}\"")
            }
        }
        return try {
            method.invoke(targetConnection, *(args ?: emptyArray()))
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }
}
