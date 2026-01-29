/*
 * SPDX-FileCopyrightText: Copyright © 2024 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.session

/**
 * This class is responsible for managing user session data within a lesson. It uses a HashMap to
 * store key-value pairs representing session data.
 */
open class LessonSession {
    private val sessionData: MutableMap<String, Any?> by lazy { mutableMapOf() }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key for the session data
     * @return the value associated with the key, or null if the key does not exist
     */
    open fun getValue(key: String): Any? = sessionData[key]

    /**
     * Sets the value for the given key. If the key already exists, its value is updated.
     *
     * @param key the key for the session data
     * @param value the value to be associated with the key
     */
    open fun setValue(
        key: String,
        value: Any?,
    ) {
        sessionData[key] = value
    }
}
