/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("webgoat-test")
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun userShouldBeSaved() {
        var user = WebGoatUser("test", "password")
        userRepository.saveAndFlush(user)

        user = requireNotNull(userRepository.findByUsername("test"))

        assertThat(user.username).isEqualTo("test")
        assertThat(user.password).isEqualTo("password")
    }
}
