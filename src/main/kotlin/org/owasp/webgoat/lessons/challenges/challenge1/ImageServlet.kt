/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge1

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
class ImageServlet {
    @RequestMapping(
        method = [GET, POST],
        value = ["/challenge/logo"],
        produces = [MediaType.IMAGE_PNG_VALUE],
    )
    @ResponseBody
    fun logo(): ByteArray {
        val bytes =
            ClassPathResource("lessons/challenges/images/webgoat2.png")
                .inputStream
                .readAllBytes()

        val pincode = PINCODE.toString().padStart(4, '0')
        pincode.forEachIndexed { index, char ->
            bytes[81216 + index] = char.code.toByte()
        }

        return bytes
    }

    companion object {
        @JvmField
        val PINCODE: Int = Random.nextInt(10000)
    }
}
