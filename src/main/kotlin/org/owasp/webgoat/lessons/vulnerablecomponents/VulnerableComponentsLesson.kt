/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.vulnerablecomponents

import com.thoughtworks.xstream.XStream
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@AssignmentHints("vulnerable.hint")
class VulnerableComponentsLesson : AssignmentEndpoint {
    @PostMapping("/VulnerableComponents/attack1")
    @ResponseBody
    fun completed(
        @RequestParam payload: String,
    ): AttackResult {
        val xstream = XStream()
        xstream.setClassLoader(Contact::class.java.classLoader)
        xstream.alias("contact", ContactImpl::class.java)
        xstream.ignoreUnknownElements()
        var contact: Contact? = null

        try {
            val cleanedPayload =
                payload
                    .replace("+", "")
                    .replace("\r", "")
                    .replace("\n", "")
                    .replace("> ", ">")
                    .replace(" <", "<")
            contact = xstream.fromXML(cleanedPayload) as Contact
        } catch (ex: Exception) {
            return failed(this).feedback("vulnerable-components.close").output(ex.message).build()
        }

        try {
            contact.firstName // trigger the example like https://x-stream.github.io/CVE-2013-7285.html
            if (contact !is ContactImpl) {
                return success(this).feedback("vulnerable-components.success").build()
            }
        } catch (e: Exception) {
            return success(this).feedback("vulnerable-components.success").output(e.message).build()
        }
        return failed(this).feedback("vulnerable-components.fromXML").feedbackArgs(contact).build()
    }
}
