/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.jwt

import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class JWTController {
    @GetMapping("/jwt")
    fun jwt(): ModelAndView = ModelAndView("jwt")

    @PostMapping(
        value = ["/jwt/decode"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun decode(
        @RequestBody formData: MultiValueMap<String, String>,
    ): JWTToken {
        val jwt = requireNotNull(formData.getFirst("token")) { "Token is required" }
        val secretKey = formData.getFirst("secretKey")
        val jwks = formData.getFirst("jwks")
        return JWTToken.decode(jwt, secretKey, jwks)
    }

    @PostMapping(
        value = ["/jwt/encode"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun encode(
        @RequestBody formData: MultiValueMap<String, String>,
    ): JWTToken {
        val header = formData.getFirst("header")
        val payload = formData.getFirst("payload")
        val secretKey = formData.getFirst("secretKey")
        return JWTToken.encode(header, payload, secretKey)
    }
}
