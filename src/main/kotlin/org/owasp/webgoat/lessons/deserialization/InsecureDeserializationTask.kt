/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.deserialization

import org.dummy.insecure.framework.VulnerableTaskHolder
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.util.Base64

@RestController
@AssignmentHints(
    "insecure-deserialization.hints.1",
    "insecure-deserialization.hints.2",
    "insecure-deserialization.hints.3",
)
class InsecureDeserializationTask : AssignmentEndpoint {
    @PostMapping("/InsecureDeserialization/task")
    @ResponseBody
    fun completed(
        @RequestParam token: String,
    ): AttackResult {
        val b64token = token.replace('-', '+').replace('_', '/')
        val before: Long
        val after: Long
        val delay: Int

        try {
            ObjectInputStream(ByteArrayInputStream(Base64.getDecoder().decode(b64token))).use { ois ->
                before = System.currentTimeMillis()
                val o = ois.readObject()
                if (o !is VulnerableTaskHolder) {
                    return if (o is String) {
                        failed(this).feedback("insecure-deserialization.stringobject").build()
                    } else {
                        failed(this).feedback("insecure-deserialization.wrongobject").build()
                    }
                }
                after = System.currentTimeMillis()
            }
        } catch (e: InvalidClassException) {
            return failed(this).feedback("insecure-deserialization.invalidversion").build()
        } catch (e: IllegalArgumentException) {
            return failed(this).feedback("insecure-deserialization.expired").build()
        } catch (e: Exception) {
            return failed(this).feedback("insecure-deserialization.invalidversion").build()
        }

        delay = (after - before).toInt()
        if (delay > 7000) {
            return failed(this).build()
        }
        if (delay < 3000) {
            return failed(this).build()
        }
        return success(this).build()
    }
}
