/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss.mitigation

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
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
@AssignmentHints(
    value = [
        "xss-mitigation-3-hint1",
        "xss-mitigation-3-hint2",
        "xss-mitigation-3-hint3",
        "xss-mitigation-3-hint4",
    ],
)
class CrossSiteScriptingLesson3 : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/attack3")
    @ResponseBody
    fun completed(
        @RequestParam editor: String,
    ): AttackResult {
        if (editor.isEmpty()) return failed(this).feedback("xss-mitigation-3-no-code").build()

        val unescapedString = Parser.unescapeEntities(editor, true)
        return try {
            val doc = Jsoup.parse(unescapedString)
            val lines = unescapedString.split("<html>")
            val include = lines[0]
            val firstNameElement =
                doc
                    .select("body > table > tbody > tr:nth-child(1) > td:nth-child(2)")
                    .first()
                    ?.text()
                    .orEmpty()
            val lastNameElement =
                doc
                    .select("body > table > tbody > tr:nth-child(2) > td:nth-child(2)")
                    .first()
                    ?.text()
                    .orEmpty()

            val includeCorrect =
                include.contains("<%@") &&
                    include.contains("taglib") &&
                    include.contains("""uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project"""") &&
                    include.contains("%>")
            val firstNameCorrect = firstNameElement == "\${e:forHtml(param.first_name)}"
            val lastNameCorrect = lastNameElement == "\${e:forHtml(param.last_name)}"

            if (includeCorrect && firstNameCorrect && lastNameCorrect) {
                success(this).feedback("xss-mitigation-3-success").build()
            } else {
                failed(this).feedback("xss-mitigation-3-failure").build()
            }
        } catch (e: Exception) {
            failed(this).output(e.message).build()
        }
    }
}
