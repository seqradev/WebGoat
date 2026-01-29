/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.URI
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

@RestController
@AssignmentHints(
    value = [
        "SqlStringInjectionHint-mitigation-10b-1",
        "SqlStringInjectionHint-mitigation-10b-2",
        "SqlStringInjectionHint-mitigation-10b-3",
        "SqlStringInjectionHint-mitigation-10b-4",
        "SqlStringInjectionHint-mitigation-10b-5",
    ],
)
class SqlInjectionLesson10b : AssignmentEndpoint {
    @PostMapping("/SqlInjectionMitigations/attack10b")
    @ResponseBody
    fun completed(
        @RequestParam editor: String,
    ): AttackResult {
        try {
            if (editor.isEmpty()) return failed(this).feedback("sql-injection.10b.no-code").build()

            val cleanedEditor = editor.replace(Regex("\\<.*?>"), "")

            val regexSetsUpConnection = "(?=.*getConnection.*)"
            val regexUsesPreparedStatement = "(?=.*PreparedStatement.*)"
            val regexUsesPlaceholder = "(?=.*\\=\\?.*|.*\\=\\s\\?.*)"
            val regexUsesSetString = "(?=.*setString.*)"
            val regexUsesExecute = "(?=.*execute.*)"
            val regexUsesExecuteUpdate = "(?=.*executeUpdate.*)"

            val codeline = cleanedEditor.replace("\n", "").replace("\r", "")

            val setsUpConnection = checkText(regexSetsUpConnection, codeline)
            val usesPreparedStatement = checkText(regexUsesPreparedStatement, codeline)
            val usesSetString = checkText(regexUsesSetString, codeline)
            val usesPlaceholder = checkText(regexUsesPlaceholder, codeline)
            val usesExecute = checkText(regexUsesExecute, codeline)
            val usesExecuteUpdate = checkText(regexUsesExecuteUpdate, codeline)

            val hasImportant =
                (
                    setsUpConnection &&
                        usesPreparedStatement &&
                        usesPlaceholder &&
                        usesSetString &&
                        (usesExecute || usesExecuteUpdate)
                )
            val hasCompiled = compileFromString(cleanedEditor)

            return if (hasImportant && hasCompiled.isEmpty()) {
                success(this).feedback("sql-injection.10b.success").build()
            } else if (hasCompiled.isNotEmpty()) {
                var errors = ""
                for (d in hasCompiled) {
                    errors += d.getMessage(null) + "<br>"
                }
                failed(this).feedback("sql-injection.10b.compiler-errors").output(errors).build()
            } else {
                failed(this).feedback("sql-injection.10b.failed").build()
            }
        } catch (e: Exception) {
            return failed(this).output(e.message).build()
        }
    }

    private fun compileFromString(s: String): List<Diagnostic<out JavaFileObject>> {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnosticsCollector = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null)
        val javaObjectFromString = getJavaFileContentsAsString(s)
        val fileObjects = listOf(javaObjectFromString)
        val task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, fileObjects)
        task.call()
        return diagnosticsCollector.diagnostics
    }

    private fun getJavaFileContentsAsString(s: String): SimpleJavaFileObject {
        val javaFileContents =
            StringBuilder(
                "import java.sql.*; public class TestClass { static String DBUSER; static String DBPW;" +
                    " static String DBURL; public static void main(String[] args) {" +
                    s +
                    "}}",
            )
        return JavaObjectFromString("TestClass.java", javaFileContents.toString())
    }

    inner class JavaObjectFromString(
        className: String,
        private val contents: String,
    ) : SimpleJavaFileObject(URI(className), JavaFileObject.Kind.SOURCE) {
        @Throws(IOException::class)
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = contents
    }

    private fun checkText(
        regex: String,
        text: String,
    ): Boolean = Regex(regex, RegexOption.IGNORE_CASE).containsMatchIn(text)
}
