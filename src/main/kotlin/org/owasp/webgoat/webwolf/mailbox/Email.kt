/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.mailbox

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
class Email(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @JsonIgnore
    var time: LocalDateTime = LocalDateTime.now(),
    @Column(length = 1024)
    var contents: String? = null,
    var sender: String? = null,
    var title: String? = null,
    var recipient: String? = null,
) : Serializable {
    val summary: String
        get() = contents?.let { "-" + it.substring(0, minOf(50, it.length)) } ?: ""

    val timestamp: LocalDateTime
        get() = time

    fun getTime(): String = DateTimeFormatter.ofPattern("h:mm a").format(time)

    val shortSender: String
        get() = sender?.substringBefore("@") ?: ""
}
