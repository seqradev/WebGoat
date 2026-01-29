/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.vulnerablecomponents

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.StreamException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VulnerableComponentsLessonTest {
    private val strangeContact =
        """
        <contact class='dynamic-proxy'>
        <interface>org.owasp.webgoat.vulnerablecomponents.Contact</interface>
          <handler class='java.beans.EventHandler'>
            <target class='java.lang.ProcessBuilder'>
              <command>
                <string>calc.exe</string>
              </command>
            </target>
            <action>start</action>
          </handler>
        </contact>
        """.trimIndent()

    private val contact = "<contact>\n</contact>"

    @Test
    fun testTransformation() {
        val xstream = XStream()
        xstream.setClassLoader(Contact::class.java.classLoader)
        xstream.alias("contact", ContactImpl::class.java)
        xstream.ignoreUnknownElements()
        assertThat(xstream.fromXML(contact)).isNotNull()
    }

    @Test
    @Disabled
    fun testIllegalTransformation() {
        val xstream = XStream()
        xstream.setClassLoader(Contact::class.java.classLoader)
        xstream.alias("contact", ContactImpl::class.java)
        xstream.ignoreUnknownElements()
        val e =
            assertThrows(RuntimeException::class.java) {
                (xstream.fromXML(strangeContact) as Contact).firstName
            }
        assertThat(e.cause?.message?.contains("calc.exe")).isTrue()
    }

    @Test
    fun testIllegalPayload() {
        val xstream = XStream()
        xstream.setClassLoader(Contact::class.java.classLoader)
        xstream.alias("contact", ContactImpl::class.java)
        xstream.ignoreUnknownElements()
        val e =
            assertThrows(StreamException::class.java) {
                (xstream.fromXML("bullssjfs") as Contact).firstName
            }
        assertThat(e.cause?.message?.contains("START_DOCUMENT")).isTrue()
    }
}
