/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.idor

class UserProfile() {
    var userId: String? = null
    var name: String? = null
    var color: String? = null
    var size: String? = null
    var isAdmin: Boolean = false
    var role: Int = 0

    constructor(id: String) : this() {
        setProfileFromId(id)
    }

    private fun setProfileFromId(id: String) {
        // emulate look up from database
        when (id) {
            "2342384" -> {
                userId = id
                color = "yellow"
                name = "Tom Cat"
                size = "small"
                isAdmin = false
                role = 3
            }
            "2342388" -> {
                userId = id
                color = "brown"
                name = "Buffalo Bill"
                size = "large"
                isAdmin = false
                role = 3
            }
            // not found
        }
    }

    fun profileToMap(): Map<String, Any?> =
        mapOf(
            "userId" to userId,
            "name" to name,
            "color" to color,
            "size" to size,
            "role" to role,
        )

    fun toHTMLString(): String =
        buildString {
            val br = "<br/>"
            append("userId$userId$br")
            append("name$name$br")
            append("size$size$br")
            append("role$role$br")
            append("isAdmin$isAdmin")
        }
}
