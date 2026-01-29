/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.mailbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.owasp.webgoat.webwolf.WebSecurityConfig
import org.owasp.webgoat.webwolf.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@WebMvcTest(MailboxController::class)
@Import(WebSecurityConfig::class)
class MailboxControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var mailbox: MailboxRepository

    @MockitoBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockitoBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @JsonIgnoreProperties("time")
    class EmailMixIn

    @BeforeEach
    fun setup() {
        objectMapper.addMixIn(Email::class.java, EmailMixIn::class.java)
    }

    @Test
    fun sendingMailShouldStoreIt() {
        val email =
            Email(
                contents = "This is a test mail",
                recipient = "test1234@webgoat.org",
                sender = "hacker@webgoat.org",
                title = "Click this mail",
                time = LocalDateTime.now(),
            )
        mvc
            .perform(
                post("/mail")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(email)),
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(username = "test1234")
    fun userShouldBeAbleToReadOwnEmail() {
        val email =
            Email(
                contents = "This is a test mail",
                recipient = "test1234@webgoat.org",
                sender = "hacker@webgoat.org",
                title = "Click this mail",
                time = LocalDateTime.now(),
            )
        `when`(mailbox.findByRecipientOrderByTimeDesc("test1234"))
            .thenReturn(listOf(email))

        mvc
            .perform(get("/mail"))
            .andExpect(status().isOk)
            .andExpect(view().name("mailbox"))
            .andExpect(content().string(containsString("Click this mail")))
            .andExpect(
                content()
                    .string(
                        containsString(
                            DateTimeFormatter.ofPattern("h:mm a").format(email.timestamp),
                        ),
                    ),
            )
    }

    @Test
    @WithMockUser(username = "test1233")
    fun differentUserShouldNotBeAbleToReadOwnEmail() {
        val email =
            Email(
                contents = "This is a test mail",
                recipient = "test1234@webgoat.org",
                sender = "hacker@webgoat.org",
                title = "Click this mail",
                time = LocalDateTime.now(),
            )
        `when`(mailbox.findByRecipientOrderByTimeDesc("test1234"))
            .thenReturn(listOf(email))

        mvc
            .perform(get("/mail"))
            .andExpect(status().isOk)
            .andExpect(view().name("mailbox"))
            .andExpect(content().string(not(containsString("Click this mail"))))
    }
}
