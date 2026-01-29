/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import javax.xml.bind.DatatypeConverter

@RestController
@AssignmentHints(
    "crypto-signing.hints.1",
    "crypto-signing.hints.2",
    "crypto-signing.hints.3",
    "crypto-signing.hints.4",
)
class SigningAssignment : AssignmentEndpoint {
    @RequestMapping(path = ["/crypto/signing/getprivate"], produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun getPrivateKey(request: HttpServletRequest): String {
        var privateKey = request.session.getAttribute("privateKeyString") as? String
        if (privateKey == null) {
            val keyPair = CryptoUtil.generateKeyPair()
            privateKey = CryptoUtil.getPrivateKeyInPEM(keyPair)
            request.session.setAttribute("privateKeyString", privateKey)
            request.session.setAttribute("keyPair", keyPair)
        }
        return privateKey
    }

    @PostMapping("/crypto/signing/verify")
    @ResponseBody
    fun completed(
        request: HttpServletRequest,
        @RequestParam modulus: String,
        @RequestParam signature: String,
    ): AttackResult {
        // used to validate the modulus of the public key but might need to be corrected
        var tempModulus = modulus
        val keyPair = request.session.getAttribute("keyPair") as KeyPair
        val rsaPubKey = keyPair.public as RSAPublicKey
        if (tempModulus.length == 512) {
            tempModulus = "00".plus(tempModulus)
        }
        val expectedModulus = DatatypeConverter.printHexBinary(rsaPubKey.modulus.toByteArray())
        if (expectedModulus != tempModulus.uppercase()) {
            log.warn("modulus {} incorrect", modulus)
            return failed(this).feedback("crypto-signing.modulusnotok").build()
        }
        // original modulus must be used otherwise the signature would be invalid
        return if (CryptoUtil.verifyMessage(modulus, signature, keyPair.public)) {
            success(this).feedback("crypto-signing.success").build()
        } else {
            log.warn("signature incorrect")
            failed(this).feedback("crypto-signing.notok").build()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SigningAssignment::class.java)
    }
}
