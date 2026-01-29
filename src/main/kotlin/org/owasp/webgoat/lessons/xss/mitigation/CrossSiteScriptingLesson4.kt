/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss.mitigation

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
@AssignmentHints(value = ["xss-mitigation-4-hint1"])
class CrossSiteScriptingLesson4 : AssignmentEndpoint {
    @PostMapping("/CrossSiteScripting/attack4")
    @ResponseBody
    fun completed(
        @RequestParam editor2: String,
    ): AttackResult {
        val editor = editor2.replace("""\<.*?>""".toRegex(), "")

        return if ((
                editor.contains("""Policy.getInstance("antisamy-slashdot.xml"""") ||
                    editor.contains(""".scan(newComment, "antisamy-slashdot.xml"""") ||
                    editor.contains(""".scan(newComment, new File("antisamy-slashdot.xml")""")
            ) &&
            editor.contains("new AntiSamy();") &&
            editor.contains(".scan(newComment,") &&
            editor.contains("CleanResults") &&
            editor.contains("MyCommentDAO.addComment(threadID, userID") &&
            editor.contains(".getCleanHTML());")
        ) {
            success(this).feedback("xss-mitigation-4-success").build()
        } else {
            failed(this).feedback("xss-mitigation-4-failed").build()
        }
    }
}
