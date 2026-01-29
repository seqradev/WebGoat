/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

@RestController
@AssignmentHints("crypto-hashing.hints.1", "crypto-hashing.hints.2")
class HashingAssignment : AssignmentEndpoint {
    @RequestMapping(path = ["/crypto/hashing/md5"], produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getMd5(request: HttpServletRequest): String {
        var md5Hash = request.session.getAttribute("md5Hash") as? String
        if (md5Hash == null) {
            val secret = SECRETS.random()

            val md = MessageDigest.getInstance("MD5")
            md.update(secret.toByteArray())
            val digest = md.digest()
            md5Hash = DatatypeConverter.printHexBinary(digest).uppercase()
            request.session.setAttribute("md5Hash", md5Hash)
            request.session.setAttribute("md5Secret", secret)
        }
        return md5Hash
    }

    @RequestMapping(path = ["/crypto/hashing/sha256"], produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getSha256(request: HttpServletRequest): String {
        var sha256 = request.session.getAttribute("sha256") as? String
        if (sha256 == null) {
            val secret = SECRETS.random()
            sha256 = getHash(secret, "SHA-256")
            request.session.setAttribute("sha256Hash", sha256)
            request.session.setAttribute("sha256Secret", secret)
        }
        return sha256
    }

    @PostMapping("/crypto/hashing")
    @ResponseBody
    fun completed(
        request: HttpServletRequest,
        @RequestParam answer_pwd1: String?,
        @RequestParam answer_pwd2: String?,
    ): AttackResult {
        val md5Secret = request.session.getAttribute("md5Secret") as? String
        val sha256Secret = request.session.getAttribute("sha256Secret") as? String

        if (answer_pwd1 != null && answer_pwd2 != null) {
            if (answer_pwd1 == md5Secret && answer_pwd2 == sha256Secret) {
                return success(this).feedback("crypto-hashing.success").build()
            } else if (answer_pwd1 == md5Secret || answer_pwd2 == sha256Secret) {
                return failed(this).feedback("crypto-hashing.oneok").build()
            }
        }
        return failed(this).feedback("crypto-hashing.empty").build()
    }

    companion object {
        @JvmField
        val SECRETS = arrayOf("secret", "admin", "password", "123456", "passw0rd")

        @JvmStatic
        fun getHash(
            secret: String,
            algorithm: String,
        ): String {
            val md = MessageDigest.getInstance(algorithm)
            md.update(secret.toByteArray())
            val digest = md.digest()
            return DatatypeConverter.printHexBinary(digest).uppercase()
        }
    }
}
