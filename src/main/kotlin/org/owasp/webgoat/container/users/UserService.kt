/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.flywaydb.core.Flyway
import org.owasp.webgoat.container.lessons.Initializable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.function.Function

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userTrackerRepository: UserProgressRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val flywayLessons: Function<String, Flyway>,
    private val lessonInitializables: List<Initializable>,
) : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): WebGoatUser {
        val webGoatUser =
            userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("User not found")
        webGoatUser.createUser()
        // TODO maybe better to use an event to initialize lessons to keep dependencies low
        lessonInitializables.forEach { it.initialize(webGoatUser) }
        return webGoatUser
    }

    fun addUser(
        username: String,
        password: String,
    ) {
        // get user if there exists one by the name
        val userAlreadyExists = userRepository.existsByUsername(username)
        val webGoatUser = userRepository.save(WebGoatUser(username, password))

        if (!userAlreadyExists) {
            // if user previously existed it will not get another tracker
            userTrackerRepository.save(UserProgress(username))
            createLessonsForUser(webGoatUser)
        }
    }

    private fun createLessonsForUser(webGoatUser: WebGoatUser) {
        val username = requireNotNull(webGoatUser.username) { "Username cannot be null" }
        jdbcTemplate.execute("CREATE SCHEMA \"$username\" authorization dba")
        flywayLessons.apply(username).migrate()
    }

    fun getAllUsers(): List<WebGoatUser> = userRepository.findAll()
}
