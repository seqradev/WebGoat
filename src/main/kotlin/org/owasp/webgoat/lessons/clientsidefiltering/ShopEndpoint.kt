/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clientSideFiltering/challenge-store")
class ShopEndpoint {
    class CheckoutCodes(
        val codes: List<CheckoutCode>,
    ) {
        fun get(code: String): CheckoutCode? = codes.find { it.code == code }
    }

    data class CheckoutCode(
        val code: String,
        val discount: Int,
    )

    private val checkoutCodes: CheckoutCodes

    init {
        val codes =
            listOf(
                CheckoutCode("webgoat", 25),
                CheckoutCode("owasp", 25),
                CheckoutCode("owasp-webgoat", 50),
            )
        checkoutCodes = CheckoutCodes(codes)
    }

    @GetMapping(value = ["/coupons/{code}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDiscountCode(
        @PathVariable code: String,
    ): CheckoutCode =
        if (ClientSideFilteringFreeAssignment.SUPER_COUPON_CODE == code) {
            CheckoutCode(ClientSideFilteringFreeAssignment.SUPER_COUPON_CODE, 100)
        } else {
            checkoutCodes.get(code) ?: CheckoutCode("no", 0)
        }

    @GetMapping(value = ["/coupons"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun all(): CheckoutCodes {
        val all =
            checkoutCodes.codes +
                CheckoutCode(ClientSideFilteringFreeAssignment.SUPER_COUPON_CODE, 100)
        return CheckoutCodes(all)
    }
}
