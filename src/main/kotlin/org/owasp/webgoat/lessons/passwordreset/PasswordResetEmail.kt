/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import java.io.Serializable
import java.time.LocalDateTime

data class PasswordResetEmail(
    val time: LocalDateTime? = null,
    val contents: String? = null,
    val sender: String? = null,
    val title: String? = null,
    val recipient: String? = null,
) : Serializable {
    class Builder {
        private var time: LocalDateTime? = null
        private var contents: String? = null
        private var sender: String? = null
        private var title: String? = null
        private var recipient: String? = null

        fun time(time: LocalDateTime?) = apply { this.time = time }

        fun contents(contents: String?) = apply { this.contents = contents }

        fun sender(sender: String?) = apply { this.sender = sender }

        fun title(title: String?) = apply { this.title = title }

        fun recipient(recipient: String?) = apply { this.recipient = recipient }

        fun build() =
            PasswordResetEmail(
                time = time,
                contents = contents,
                sender = sender,
                title = title,
                recipient = recipient,
            )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
