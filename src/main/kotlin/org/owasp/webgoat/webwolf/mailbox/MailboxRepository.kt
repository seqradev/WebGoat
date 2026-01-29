/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.mailbox

import org.springframework.data.jpa.repository.JpaRepository

interface MailboxRepository : JpaRepository<Email, String> {
    fun findByRecipientOrderByTimeDesc(recipient: String): List<Email>
}
