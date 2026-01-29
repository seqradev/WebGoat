/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.spoofcookie.encoders

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class EncDecTest {
    @ParameterizedTest
    @DisplayName("Encode test")
    @MethodSource("providedForEncValues")
    fun testEncode(
        decoded: String,
        encoded: String,
    ) {
        val result = requireNotNull(EncDec.encode(decoded))

        assertThat(result.endsWith(encoded)).isTrue()
    }

    @ParameterizedTest
    @DisplayName("Decode test")
    @MethodSource("providedForDecValues")
    fun testDecode(
        decoded: String,
        encoded: String,
    ) {
        val result = EncDec.decode(encoded)

        assertThat(decoded, `is`(result))
    }

    @Test
    @DisplayName("null encode test")
    fun testNullEncode() {
        assertThat(EncDec.encode(null)).isNull()
    }

    @Test
    @DisplayName("null decode test")
    fun testNullDecode() {
        assertThat(EncDec.decode(null)).isNull()
    }

    companion object {
        @JvmStatic
        fun providedForEncValues(): Stream<Arguments> =
            Stream.of(
                Arguments.of("webgoat", "YxNmY2NzYyNjU3Nw=="),
                Arguments.of("admin", "2ZTY5NmQ2NDYx"),
                Arguments.of("tom", "2ZDZmNzQ="),
            )

        @JvmStatic
        fun providedForDecValues(): Stream<Arguments> =
            Stream.of(
                Arguments.of("webgoat", "NjI2MTcwNGI3YTQxNGE1OTU2NzQ3NDYxNmY2NzYyNjU3Nw=="),
                Arguments.of("admin", "NjI2MTcwNGI3YTQxNGE1OTU2NzQ2ZTY5NmQ2NDYx"),
                Arguments.of("tom", "NjI2MTcwNGI3YTQxNGE1OTU2NzQ2ZDZmNzQ="),
            )
    }
}
