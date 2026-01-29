/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.webwolfintroduction

import java.io.Serializable

data class Email(
    val contents: String?,
    val sender: String?,
    val title: String?,
    val recipient: String?,
) : Serializable {
    class Builder {
        private var contents: String? = null
        private var sender: String? = null
        private var title: String? = null
        private var recipient: String? = null

        fun contents(contents: String?) = apply { this.contents = contents }

        fun sender(sender: String?) = apply { this.sender = sender }

        fun title(title: String?) = apply { this.title = title }

        fun recipient(recipient: String?) = apply { this.recipient = recipient }

        fun build(): Email = Email(contents, sender, title, recipient)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
