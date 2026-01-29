/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.ssrf

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets

@RestController
@AssignmentHints("ssrf.hint3")
class SSRFTask2 : AssignmentEndpoint {
    @PostMapping("/SSRF/task2")
    @ResponseBody
    fun completed(
        @RequestParam url: String,
    ): AttackResult = furBall(url)

    protected fun furBall(url: String): AttackResult {
        if (url.matches(Regex("http://ifconfig\\.pro"))) {
            val html: String
            try {
                URL(url).openStream().use { input ->
                    html =
                        String(input.readAllBytes(), StandardCharsets.UTF_8)
                            .replace("\n", "<br>") // Otherwise the \n gets escaped in the response
                }
            } catch (e: MalformedURLException) {
                return getFailedResult(e.message ?: "")
            } catch (e: IOException) {
                // in case the external site is down, the test and lesson should still be ok
                return success(this)
                    .feedback("ssrf.success")
                    .output(
                        "<html><body>Although the http://ifconfig.pro site is down, you still managed to solve" +
                            " this exercise the right way!</body></html>",
                    ).build()
            }
            return success(this).feedback("ssrf.success").output(html).build()
        }
        val html = "<img class=\"image\" alt=\"image post\" src=\"images/cat.jpg\">"
        return getFailedResult(html)
    }

    private fun getFailedResult(errorMsg: String): AttackResult =
        failed(this).feedback("ssrf.failure").output(errorMsg).build()
}
