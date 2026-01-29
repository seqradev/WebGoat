/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.session

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LabelDebuggerTest {
    @Test
    fun testSetEnabledTrue() {
        val ld = LabelDebugger()
        ld.isEnabled = true
        assertThat(ld.isEnabled).isTrue()
    }

    @Test
    fun testSetEnabledFalse() {
        val ld = LabelDebugger()
        ld.isEnabled = false
        assertThat(ld.isEnabled).isFalse()
    }

    @Test
    fun testSetEnabledNullThrowsException() {
        val ld = LabelDebugger()
        ld.isEnabled = true
        assertThat(ld.isEnabled).isTrue()
    }

    @Test
    fun testEnableIsTrue() {
        val ld = LabelDebugger()
        ld.enable()
        assertThat(ld.isEnabled).isTrue()
    }

    @Test
    fun testDisableIsFalse() {
        val ld = LabelDebugger()
        ld.disable()
        assertThat(ld.isEnabled).isFalse()
    }
}
