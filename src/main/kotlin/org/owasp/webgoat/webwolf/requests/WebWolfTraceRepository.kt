/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests

import com.google.common.collect.EvictingQueue
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository

/**
 * Keep track of all the incoming requests, we are only keeping track of request originating from
 * WebGoat.
 */
class WebWolfTraceRepository : HttpExchangeRepository {
    private enum class MatchingMode {
        CONTAINS,
        ENDS_WITH,
        EQUALS,
    }

    private data class Exclusion(
        val path: String,
        val mode: MatchingMode,
    ) {
        fun matches(path: String): Boolean =
            when (mode) {
                MatchingMode.CONTAINS -> path.contains(this.path)
                MatchingMode.ENDS_WITH -> path.endsWith(this.path)
                MatchingMode.EQUALS -> path == this.path
            }

        companion object {
            fun contains(exclusionPattern: String) = Exclusion(exclusionPattern, MatchingMode.CONTAINS)

            fun endsWith(exclusionPattern: String) = Exclusion(exclusionPattern, MatchingMode.ENDS_WITH)
        }
    }

    private val traces: EvictingQueue<HttpExchange> = EvictingQueue.create(10000)
    private val exclusionList =
        listOf(
            Exclusion.contains("/tmpdir"),
            Exclusion.contains("/home"),
            Exclusion.endsWith("/files"),
            Exclusion.contains("/images/"),
            Exclusion.contains("/js/"),
            Exclusion.contains("/webjars/"),
            Exclusion.contains("/requests"),
            Exclusion.contains("/css/"),
            Exclusion.contains("/mail"),
        )

    override fun findAll(): List<HttpExchange> = ArrayList(traces)

    private fun isInExclusionList(path: String): Boolean = exclusionList.any { it.matches(path) }

    override fun add(httpTrace: HttpExchange) {
        val path = httpTrace.request.uri.path
        if (!isInExclusionList(path)) {
            traces.add(httpTrace)
        }
    }
}
