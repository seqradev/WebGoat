/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.owasp.webgoat.container.LessonDataSource
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class MissingAccessControlUserRepository(
    lessonDataSource: LessonDataSource,
) {
    private val jdbcTemplate = NamedParameterJdbcTemplate(lessonDataSource)
    private val mapper =
        RowMapper { rs, _ ->
            User(rs.getString("username"), rs.getString("password"), rs.getBoolean("admin"))
        }

    fun findAllUsers(): List<User> =
        jdbcTemplate.query("select username, password, admin from access_control_users", mapper)

    fun findByUsername(username: String): User? {
        val users =
            jdbcTemplate.query(
                "select username, password, admin from access_control_users where username=:username",
                MapSqlParameterSource().addValue("username", username),
                mapper,
            )
        return users.firstOrNull()
    }

    fun save(user: User): User {
        jdbcTemplate.update(
            "INSERT INTO access_control_users(username, password, admin) VALUES(:username,:password,:admin)",
            MapSqlParameterSource()
                .addValue("username", user.username)
                .addValue("password", user.password)
                .addValue("admin", user.isAdmin),
        )
        return user
    }
}
