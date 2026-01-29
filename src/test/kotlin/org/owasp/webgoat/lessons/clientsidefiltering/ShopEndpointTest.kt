/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class ShopEndpointTest : LessonTest() {
    override lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = standaloneSetup(ShopEndpoint()).build()
    }

    @Test
    fun getSuperCoupon() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/clientSideFiltering/challenge-store/coupons/${ClientSideFilteringFreeAssignment.SUPER_COUPON_CODE}",
                ),
            ).andExpect(jsonPath("$.code", CoreMatchers.`is`(ClientSideFilteringFreeAssignment.SUPER_COUPON_CODE)))
            .andExpect(jsonPath("$.discount", CoreMatchers.`is`(100)))
    }

    @Test
    fun getCoupon() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/clientSideFiltering/challenge-store/coupons/webgoat"))
            .andExpect(jsonPath("$.code", CoreMatchers.`is`("webgoat")))
            .andExpect(jsonPath("$.discount", CoreMatchers.`is`(25)))
    }

    @Test
    fun askForUnknownCouponCode() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/clientSideFiltering/challenge-store/coupons/does-not-exists",
                ),
            ).andExpect(jsonPath("$.code", CoreMatchers.`is`("no")))
            .andExpect(jsonPath("$.discount", CoreMatchers.`is`(0)))
    }

    @Test
    fun fetchAllTheCouponsShouldContainGetItForFree() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/clientSideFiltering/challenge-store/coupons"))
            .andExpect(jsonPath("$.codes[3].code", `is`("get_it_for_free")))
    }
}
