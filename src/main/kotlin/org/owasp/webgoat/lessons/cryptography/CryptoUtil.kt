/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography

import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAKeyGenParameterSpec
import java.util.Base64
import javax.xml.bind.DatatypeConverter

object CryptoUtil {
    private val log = LoggerFactory.getLogger(CryptoUtil::class.java)

    private val FERMAT_PRIMES =
        arrayOf(
            BigInteger.valueOf(3),
            BigInteger.valueOf(5),
            BigInteger.valueOf(17),
            BigInteger.valueOf(257),
            BigInteger.valueOf(65537),
        )

    @JvmStatic
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val kpgSpec = RSAKeyGenParameterSpec(2048, FERMAT_PRIMES.random())
        keyPairGenerator.initialize(kpgSpec)
        return keyPairGenerator.generateKeyPair()
    }

    @JvmStatic
    fun getPrivateKeyInPEM(keyPair: KeyPair): String =
        buildString {
            appendLine("-----BEGIN PRIVATE KEY-----")
            appendLine(String(Base64.getEncoder().encode(keyPair.private.encoded), StandardCharsets.UTF_8))
            appendLine("-----END PRIVATE KEY-----")
        }

    @JvmStatic
    fun signMessage(
        message: String,
        privateKey: PrivateKey,
    ): String? {
        log.debug("start signMessage")
        val signature =
            runCatching {
                val instance = Signature.getInstance("SHA256withRSA")
                instance.initSign(privateKey)
                instance.update(message.toByteArray(StandardCharsets.UTF_8))
                String(Base64.getEncoder().encode(instance.sign()), StandardCharsets.UTF_8)
            }.onSuccess { log.info("signed the signature with result: {}", it) }
                .onFailure { log.error("Signature signing failed", it) }
                .getOrNull()
        log.debug("end signMessage")
        return signature
    }

    @JvmStatic
    fun verifyMessage(
        message: String,
        base64EncSignature: String,
        publicKey: PublicKey,
    ): Boolean {
        log.debug("start verifyMessage")
        val result =
            runCatching {
                val cleanedSignature =
                    base64EncSignature
                        .replace("\r", "")
                        .replace("\n", "")
                        .replace(" ", "")
                val decodedSignature = Base64.getDecoder().decode(cleanedSignature)

                val instance = Signature.getInstance("SHA256withRSA")
                instance.initVerify(publicKey)
                instance.update(message.toByteArray(StandardCharsets.UTF_8))
                instance.verify(decodedSignature)
            }.onSuccess { log.info("Verified the signature with result: {}", it) }
                .onFailure { log.error("Signature verification failed", it) }
                .getOrDefault(false)
        log.debug("end verifyMessage")
        return result
    }

    @JvmStatic
    fun verifyAssignment(
        modulus: String?,
        signature: String?,
        publicKey: PublicKey,
    ): Boolean {
        // first check if the signature is correct, i.e. right private key and right hash
        var result = false
        var adjustedModulus = modulus

        if (adjustedModulus != null && signature != null) {
            result = verifyMessage(adjustedModulus, signature, publicKey)

            // next check if the submitted modulus is the correct modulus of the public key
            val rsaPubKey = publicKey as RSAPublicKey
            if (adjustedModulus.length == 512) {
                adjustedModulus = "00".plus(adjustedModulus)
            }
            val expectedModulus =
                DatatypeConverter.printHexBinary(rsaPubKey.modulus.toByteArray())
            result = result && expectedModulus == adjustedModulus.uppercase()
        }
        return result
    }

    @JvmStatic
    fun getPrivateKeyFromPEM(privateKeyPem: String): PrivateKey {
        var cleaned =
            privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")

        val decoded = Base64.getDecoder().decode(cleaned)

        val spec = PKCS8EncodedKeySpec(decoded)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }
}
