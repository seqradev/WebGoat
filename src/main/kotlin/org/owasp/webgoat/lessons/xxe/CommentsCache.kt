/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.StringReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.XMLConstants
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

@Component
@Scope("singleton")
class CommentsCache {
    class Comments : ArrayList<Comment>() {
        fun sort() {
            sortByDescending { it.dateTime }
        }
    }

    companion object {
        private val comments = Comments()
        private val userComments = mutableMapOf<WebGoatUser, Comments>()
        private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
    }

    init {
        initDefaultComments()
    }

    fun initDefaultComments() {
        comments.add(Comment("webgoat", LocalDateTime.now().format(fmt), "Silly cat...."))
        comments.add(
            Comment(
                "guest",
                LocalDateTime.now().format(fmt),
                "I think I will use this picture in one of my projects.",
            ),
        )
        comments.add(Comment("guest", LocalDateTime.now().format(fmt), "Lol!! :-)."))
    }

    fun getComments(user: WebGoatUser): Comments {
        val allComments = Comments()
        userComments[user]?.let { allComments.addAll(it) }
        allComments.addAll(comments)
        allComments.sort()
        return allComments
    }

    /**
     * Notice this parse method is not a "trick" to get the XXE working, we need to catch some of the
     * exception which might happen during when users post message (we want to give feedback track
     * progress etc). In real life the XmlMapper bean defined above will be used automatically and the
     * Comment class can be directly used in the controller method (instead of a String)
     */
    @Throws(XMLStreamException::class, JAXBException::class)
    fun parseXml(
        xml: String,
        securityEnabled: Boolean,
    ): Comment {
        val jc = JAXBContext.newInstance(Comment::class.java)
        val xif = XMLInputFactory.newInstance()

        // TODO fix me disabled for now.
        if (securityEnabled) {
            xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "") // Compliant
            xif.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "") // compliant
        }

        val xsr = xif.createXMLStreamReader(StringReader(xml))

        val unmarshaller = jc.createUnmarshaller()
        return unmarshaller.unmarshal(xsr) as Comment
    }

    fun addComment(
        comment: Comment,
        user: WebGoatUser,
        visibleForAllUsers: Boolean,
    ) {
        comment.dateTime = LocalDateTime.now().format(fmt)
        comment.user = user.username
        if (visibleForAllUsers) {
            comments.add(comment)
        } else {
            val comments = userComments.getOrPut(user) { Comments() }
            comments.add(comment)
        }
    }

    fun reset(user: WebGoatUser) {
        comments.clear()
        userComments.remove(user)
        initDefaultComments()
    }
}
