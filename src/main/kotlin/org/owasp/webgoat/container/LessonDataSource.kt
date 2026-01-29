/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.owasp.webgoat.container.lessons.LessonConnectionInvocationHandler
import org.springframework.jdbc.datasource.ConnectionProxy
import java.io.PrintWriter
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.util.logging.Logger
import javax.sql.DataSource

class LessonDataSource(
    private val originalDataSource: DataSource,
) : DataSource {
    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        val targetConnection = originalDataSource.connection
        return Proxy.newProxyInstance(
            ConnectionProxy::class.java.classLoader,
            arrayOf<Class<*>>(ConnectionProxy::class.java),
            LessonConnectionInvocationHandler(targetConnection),
        ) as Connection
    }

    @Throws(SQLException::class)
    override fun getConnection(
        username: String,
        password: String,
    ): Connection = originalDataSource.getConnection(username, password)

    @Throws(SQLException::class)
    override fun getLogWriter(): PrintWriter = originalDataSource.logWriter

    @Throws(SQLException::class)
    override fun setLogWriter(out: PrintWriter) = run { originalDataSource.logWriter = out }

    @Throws(SQLException::class)
    override fun setLoginTimeout(seconds: Int) = run { originalDataSource.loginTimeout = seconds }

    @Throws(SQLException::class)
    override fun getLoginTimeout(): Int = originalDataSource.loginTimeout

    @Throws(SQLFeatureNotSupportedException::class)
    override fun getParentLogger(): Logger = originalDataSource.parentLogger

    @Throws(SQLException::class)
    override fun <T> unwrap(clazz: Class<T>): T = originalDataSource.unwrap(clazz)

    @Throws(SQLException::class)
    override fun isWrapperFor(clazz: Class<*>): Boolean = originalDataSource.isWrapperFor(clazz)
}
