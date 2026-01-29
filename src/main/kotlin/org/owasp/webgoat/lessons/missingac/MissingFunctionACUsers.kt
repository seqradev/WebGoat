/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_ADMIN
import org.owasp.webgoat.lessons.missingac.MissingFunctionAC.Companion.PASSWORD_SALT_SIMPLE
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView

@Controller
class MissingFunctionACUsers(
    private val userRepository: MissingAccessControlUserRepository,
) {
    private val log = LoggerFactory.getLogger(MissingFunctionACUsers::class.java)

    @GetMapping(path = ["access-control/users"])
    fun listUsers(): ModelAndView {
        val model = ModelAndView()
        model.viewName = "list_users"
        val allUsers = userRepository.findAllUsers()
        model.addObject("numUsers", allUsers.size)
        // add display user objects in place of direct users
        val displayUsers = allUsers.map { DisplayUser(it, PASSWORD_SALT_SIMPLE) }
        model.addObject("allUsers", displayUsers)
        return model
    }

    @GetMapping(path = ["access-control/users"], consumes = ["application/json"])
    @ResponseBody
    fun usersService(): ResponseEntity<List<DisplayUser>> =
        ResponseEntity.ok(
            userRepository.findAllUsers().map { DisplayUser(it, PASSWORD_SALT_SIMPLE) },
        )

    @GetMapping(path = ["access-control/users-admin-fix"], consumes = ["application/json"])
    @ResponseBody
    fun usersFixed(
        @CurrentUsername username: String?,
    ): ResponseEntity<List<DisplayUser>> {
        val currentUser = username?.let { userRepository.findByUsername(it) }
        return if (currentUser != null && currentUser.isAdmin) {
            ResponseEntity.ok(
                userRepository.findAllUsers().map { DisplayUser(it, PASSWORD_SALT_ADMIN) },
            )
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PostMapping(
        path = ["access-control/users", "access-control/users-admin-fix"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun addUser(
        @RequestBody newUser: User,
    ): User? =
        try {
            userRepository.save(newUser)
            newUser
        } catch (ex: Exception) {
            log.error("Error creating new User", ex)
            null
        }
}
