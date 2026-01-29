/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPublicKey
import javax.xml.bind.DatatypeConverter

class CryptoUtilTest {
    @Test
    fun testSigningAssignment() {
        try {
            val keyPair = CryptoUtil.generateKeyPair()
            val rsaPubKey = keyPair.public as RSAPublicKey
            val privateKey =
                CryptoUtil.getPrivateKeyFromPEM(CryptoUtil.getPrivateKeyInPEM(keyPair))
            val modulus = DatatypeConverter.printHexBinary(rsaPubKey.modulus.toByteArray())
            val signature = CryptoUtil.signMessage(modulus, privateKey)
            log.debug("public exponent {}", rsaPubKey.publicExponent)
            assertThat(CryptoUtil.verifyAssignment(modulus, signature, keyPair.public)).isTrue()
        } catch (e: Exception) {
            fail<Any>("Signing failed")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CryptoUtilTest::class.java)
    }
}
