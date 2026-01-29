/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.vulnerablecomponents

class ContactImpl : Contact {
    override var id: Int? = null
    override var firstName: String? = null
    override var lastName: String? = null
    override var email: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContactImpl) return false
        return id == other.id &&
            firstName == other.firstName &&
            lastName == other.lastName &&
            email == other.email
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "ContactImpl(id=$id, firstName=$firstName, lastName=$lastName, email=$email)"
}
