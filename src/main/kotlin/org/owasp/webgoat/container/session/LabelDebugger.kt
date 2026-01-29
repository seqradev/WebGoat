/*
 * SPDX-FileCopyrightText: Copyright © 2015 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.session

import java.io.Serializable

class LabelDebugger : Serializable {
    var isEnabled: Boolean = false

    /** Enables label debugging */
    fun enable() {
        isEnabled = true
    }

    /** Disables label debugging */
    fun disable() {
        isEnabled = false
    }
}
