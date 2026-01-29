/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.function.Function

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userTrackerRepository: UserProgressRepository

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @Mock
    private lateinit var flywayLessons: Function<String, Flyway>

    @Test
    fun shouldThrowExceptionWhenUserIsNotFound() {
        `when`(userRepository.findByUsername(anyString())).thenReturn(null)
        val userService =
            UserService(
                userRepository,
                userTrackerRepository,
                jdbcTemplate,
                flywayLessons,
                emptyList(),
            )
        assertThatThrownBy { userService.loadUserByUsername("unknown") }
            .isInstanceOf(UsernameNotFoundException::class.java)
    }
}
