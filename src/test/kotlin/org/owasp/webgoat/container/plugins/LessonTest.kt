/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.plugins

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.`when`
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.WebGoat
import org.owasp.webgoat.container.i18n.Language
import org.owasp.webgoat.container.i18n.PluginMessages
import org.owasp.webgoat.container.lessons.Initializable
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.Locale
import java.util.function.Function

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [WebGoat::class])
@TestPropertySource(
    locations = [
        "classpath:/application-webgoat.properties",
        "classpath:/application-webgoat-test.properties",
    ],
)
@WithWebGoatUser
abstract class LessonTest {
    @LocalServerPort
    protected var localPort: Int = 0

    protected open lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var wac: WebApplicationContext

    @Autowired
    protected lateinit var messages: PluginMessages

    @Autowired
    private lateinit var flywayLessons: Function<String, Flyway>

    @Autowired
    private lateinit var lessonInitializers: List<Initializable>

    @MockitoBean
    private lateinit var language: Language

    @MockitoBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @Value("\${webgoat.user.directory}")
    protected lateinit var webGoatHomeDirectory: String

    @BeforeEach
    fun init() {
        `when`(language.locale).thenReturn(Locale.getDefault())
        val user = SecurityContextHolder.getContext().authentication.principal as WebGoatUser
        flywayLessons.apply(requireNotNull(user.username)).migrate()
        lessonInitializers.forEach { it.initialize(user) }
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build()
    }
}
