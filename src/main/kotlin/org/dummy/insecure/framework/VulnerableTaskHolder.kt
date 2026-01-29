/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.dummy.insecure.framework

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.Serializable
import java.time.LocalDateTime

// TODO move back to lesson
class VulnerableTaskHolder(
    private var taskName: String,
    private var taskAction: String,
) : Serializable {
    private var requestedExecutionTime: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "VulnerableTaskHolder [taskName=$taskName, taskAction=$taskAction, requestedExecutionTime=$requestedExecutionTime]"

    @Throws(Exception::class)
    private fun readObject(stream: ObjectInputStream) {
        // unserialize data so taskName and taskAction are available
        stream.defaultReadObject()

        // do something with the data
        log.info("restoring task: {}", taskName)
        log.info("restoring time: {}", requestedExecutionTime)

        if (requestedExecutionTime != null &&
            (
                requestedExecutionTime.isBefore(LocalDateTime.now().minusMinutes(10)) ||
                    requestedExecutionTime.isAfter(LocalDateTime.now())
            )
        ) {
            // do nothing is the time is not within 10 minutes after the object has been created
            log.debug(this.toString())
            throw IllegalArgumentException("outdated")
        }

        // condition is here to prevent you from destroying the goat altogether
        if ((taskAction.startsWith("sleep") || taskAction.startsWith("ping")) &&
            taskAction.length < 22
        ) {
            log.info("about to execute: {}", taskAction)
            try {
                val p = Runtime.getRuntime().exec(taskAction)
                BufferedReader(InputStreamReader(p.inputStream)).lineSequence().forEach { log.info(it) }
            } catch (e: IOException) {
                log.error("IO Exception", e)
            }
        }
    }

    companion object {
        private const val serialVersionUID = 2L
        private val log = LoggerFactory.getLogger(VulnerableTaskHolder::class.java)
    }
}
