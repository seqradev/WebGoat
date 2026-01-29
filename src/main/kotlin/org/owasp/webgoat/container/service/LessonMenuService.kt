/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.lessons.LessonMenuItem
import org.owasp.webgoat.container.lessons.LessonMenuItemType
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LessonMenuService(
    private val course: Course,
    private val userTrackerRepository: UserProgressRepository,
    @Value("#{\"\${exclude.categories}\".split(',')}")
    private val excludeCategories: List<String>,
    @Value("#{\"\${exclude.lessons}\".split(',')}")
    private val excludeLessons: List<String>,
) {
    @RequestMapping(path = [URL_LESSONMENU_MVC], produces = ["application/json"])
    @ResponseBody
    fun showLeftNav(
        @CurrentUsername username: String?,
    ): List<LessonMenuItem> {
        val userTracker =
            requireNotNull(userTrackerRepository.findByUser(username)) {
                "User progress not found for user: $username"
            }

        return course.categories
            .filterNot { it.name in excludeCategories }
            .map { category ->
                LessonMenuItem().apply {
                    name = category.getName()
                    type = LessonMenuItemType.CATEGORY
                    course
                        .getLessons(category)
                        .sortedBy { it.getTitle() }
                        .filterNot { lesson -> lesson.getName()?.toString() in excludeLessons }
                        .forEach { lesson ->
                            val lessonItem =
                                LessonMenuItem().apply {
                                    name = lesson.getTitle()
                                    link = lesson.getLink()
                                    type = LessonMenuItemType.LESSON
                                    isComplete = userTracker.getLessonProgress(lesson).isLessonSolved
                                }
                            addChild(lessonItem)
                        }
                }
            }
    }

    companion object {
        const val URL_LESSONMENU_MVC = "/service/lessonmenu.mvc"
    }
}
