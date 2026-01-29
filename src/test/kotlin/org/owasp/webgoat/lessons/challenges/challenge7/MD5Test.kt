/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge7

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class MD5Test {
    @ParameterizedTest
    @DisplayName("MD5 test")
    @MethodSource("providedForMD5Values")
    fun testMD5(
        input: String,
        output: String,
    ) {
        assertThat(output).isEqualTo(MD5.getHashString(input.toByteArray()))
    }

    companion object {
        @JvmStatic
        fun providedForMD5Values(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", "d41d8cd98f00b204e9800998ecf8427e"),
                Arguments.of("a string", "3a315533c0f34762e0c45e3d4e9d525c"),
            )
    }
}
