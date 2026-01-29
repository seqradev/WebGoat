/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

/**
 * Abstract base class for all WebGoat lessons.
 */
abstract class Lesson {
    open var assignments: MutableList<Assignment> = mutableListOf()

    fun addAssignment(assignment: Assignment) {
        this.assignments.add(assignment)
    }

    /**
     * getName.
     *
     * @return a LessonName object.
     */
    open fun getName(): LessonName = LessonName(javaClass.simpleName)

    /**
     * Gets the category attribute of the Lesson object
     *
     * @return The category value
     */
    fun getCategory(): Category = getDefaultCategory()

    /**
     * getDefaultCategory.
     *
     * @return a Category object.
     */
    protected abstract fun getDefaultCategory(): Category

    /**
     * Gets the title attribute of the HelloScreen object
     *
     * @return The title value
     */
    abstract fun getTitle(): String

    /**
     * Returns the default "path" portion of a lesson's URL.
     *
     * Legacy webgoat lesson links are of the form "attack?Screen=Xmenu=Ystage=Z". This method
     * returns the path portion of the url, i.e., "attack" in the string above.
     *
     * Newer, Spring-Controller-based classes will override this method to return "*.do"-styled
     * paths.
     *
     * @return a String object.
     */
    protected open fun getPath(): String = "#lesson/"

    /**
     * Get the link that can be used to request this screen.
     *
     * Rendering the link in the browser may result in Javascript sending additional requests to
     * perform necessary actions or to obtain data relevant to the lesson or the element of the lesson
     * selected by the user. Thanks to using the hash mark "#" and Javascript handling the clicks, the
     * user will experience less waiting as the pages do not have to reload entirely.
     *
     * @return a String object.
     */
    fun getLink(): String = "${getPath()}${getId()}.lesson"

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    override fun toString(): String = getTitle()

    fun getId(): String = this.javaClass.simpleName

    /**
     * This is used in Thymeleaf to construct the HTML to load the lesson content from. See
     * lesson_content.html
     */
    fun getPackage(): String =
        // package name is the direct package name below lessons (any subpackage will be removed)
        javaClass.packageName
            .removePrefix("org.owasp.webgoat.lessons.")
            .substringBefore(".")
}
