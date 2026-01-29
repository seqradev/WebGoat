/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.mailbox

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
class MailboxRepositoryTest {
    @Autowired
    private lateinit var mailboxRepository: MailboxRepository

    @Test
    fun emailShouldBeSaved() {
        val email =
            Email(
                time = LocalDateTime.now(),
                title = "test",
                sender = "test@test.com",
                contents = "test",
                recipient = "someone@webwolf.org",
            )
        mailboxRepository.save(email)
    }

    @Test
    fun savedEmailShouldBeFoundByRecipient() {
        val email =
            Email(
                time = LocalDateTime.now(),
                title = "test",
                sender = "test@test.com",
                contents = "test",
                recipient = "someone@webwolf.org",
            )
        mailboxRepository.saveAndFlush(email)

        val emails = mailboxRepository.findByRecipientOrderByTimeDesc("someone@webwolf.org")

        assertThat(emails.size).isEqualTo(1)
    }
}
