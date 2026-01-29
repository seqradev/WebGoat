/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securepasswords

import com.nulabinc.zxcvbn.Zxcvbn
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@RestController
class SecurePasswordsAssignment : AssignmentEndpoint {
    @PostMapping("SecurePasswords/assignment")
    @ResponseBody
    fun completed(
        @RequestParam password: String,
    ): AttackResult {
        val zxcvbn = Zxcvbn()
        val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
        df.maximumFractionDigits = 340
        val strength = zxcvbn.measure(password)

        val output =
            buildString {
                append("<b>Your Password: *******</b></br>")
                append("<b>Length: </b>${password.length}</br>")
                append("<b>Estimated guesses needed to crack your password: </b>")
                append("${df.format(strength.guesses)}</br>")
                append("<div style=\"float: left;padding-right: 10px;\">")
                append("<b>Score: </b>${strength.score}/4 </div>")
                val color =
                    when {
                        strength.score <= 1 -> "red"
                        strength.score <= 3 -> "orange"
                        else -> "green"
                    }
                append("<div style=\"background-color:$color;width: 200px;")
                append("border-radius: 12px;float: left;\">&nbsp;</div></br>")
                val crackTime = strength.crackTimeSeconds.onlineNoThrottling10perSecond.toLong()
                append("<b>Estimated cracking time: </b>${calculateTime(crackTime)}</br>")
                if (strength.feedback.warning.isNotEmpty()) {
                    append("<b>Warning: </b>${strength.feedback.warning}</br>")
                }
                // possible feedback: https://github.com/dropbox/zxcvbn/blob/master/src/feedback.coffee
                // maybe ask user to try also weak passwords to see and understand feedback?
                if (strength.feedback.suggestions.isNotEmpty()) {
                    append("<b>Suggestions:</b></br><ul>")
                    append(strength.feedback.suggestions.joinToString("") { "<li>$it</li>" })
                    append("</ul></br>")
                }
                append("<b>Score: </b>${strength.score}/4 </br>")
            }

        return if (strength.score >= 4) {
            success(this).feedback("securepassword-success").output(output).build()
        } else {
            failed(this).feedback("securepassword-failed").output(output).build()
        }
    }

    companion object {
        @JvmStatic
        fun calculateTime(seconds: Long): String {
            val s = 1
            val min = 60 * s
            val hr = 60 * min
            val d = 24 * hr
            val yr = 365 * d

            val years = seconds / d / 365
            val days = (seconds % yr) / d
            val hours = (seconds % d) / hr
            val minutes = (seconds % hr) / min
            val sec = (seconds % min) * s

            return "$years years $days days $hours hours $minutes minutes $sec seconds"
        }
    }
}
