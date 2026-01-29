/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import org.junit.jupiter.api.Test

class XSSIntegrationTest : IntegrationTest() {
    @Test
    fun crossSiteScriptingAssignments() {
        startLesson("CrossSiteScripting")

        var params = mutableMapOf<String, Any>()
        params["checkboxAttack1"] = "value"
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/attack1"), params, true)

        params = mutableMapOf()
        params["QTY1"] = "1"
        params["QTY2"] = "1"
        params["QTY3"] = "1"
        params["QTY4"] = "1"
        params["field1"] = "<script>alert('XSS+Test')</script>"
        params["field2"] = "111"
        checkAssignmentWithGet(webGoatUrlConfig.url("CrossSiteScripting/attack5a"), params, true)

        params = mutableMapOf()
        params["DOMTestRoute"] = "start.mvc#test"
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/attack6a"), params, true)

        params = mutableMapOf()
        params["param1"] = "42"
        params["param2"] = "24"

        val result: String =
            RestAssured
                .given()
                .`when`()
                .relaxedHTTPSValidation()
                .cookie("JSESSIONID", webGoatCookie)
                .header("webgoat-requested-by", "dom-xss-vuln")
                .header("X-Requested-With", "XMLHttpRequest")
                .formParams(params)
                .post(webGoatUrlConfig.url("CrossSiteScripting/phone-home-xss"))
                .then()
                .statusCode(200)
                .extract()
                .path("output")
        val secretNumber = result.substring("phoneHome Response is ".length)

        params = mutableMapOf()
        params["successMessage"] = secretNumber
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/dom-follow-up"), params, true)

        params = mutableMapOf()
        params["question_0_solution"] =
            "Solution 4: No because the browser trusts the website if it is acknowledged trusted, then the browser does not know that the script is malicious."
        params["question_1_solution"] =
            "Solution 3: The data is included in dynamic content that is sent to a web user without being validated for malicious content."
        params["question_2_solution"] =
            "Solution 1: The script is permanently stored on the server and the victim gets the malicious script when requesting information from the server."
        params["question_3_solution"] =
            "Solution 2: They reflect the injected script off the web server. That occurs when input sent to the web server is part of the request."
        params["question_4_solution"] =
            "Solution 4: No there are many other ways. Like HTML, Flash or any other type of code that the browser executes."
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/quiz"), params, true)

        params = mutableMapOf()
        params["editor"] =
            "<%@ taglib uri=\"https://www.owasp.org/index.php/OWASP_Java_Encoder_Project\" %>" +
            "<html>" +
            "<head>" +
            "<title>Using GET and POST Method to Read Form Data</title>" +
            "</head>" +
            "<body>" +
            "<h1>Using POST Method to Read Form Data</h1>" +
            "<table>" +
            "<tbody>" +
            "<tr>" +
            "<td><b>First Name:</b></td>" +
            "<td>\${e:forHtml(param.first_name)}</td>" +
            "</tr>" +
            "<tr>" +
            "<td><b>Last Name:</b></td>" +
            "<td>\${e:forHtml(param.last_name)}</td>" +
            "</tr>" +
            "</tbody>" +
            "</table>" +
            "</body>" +
            "</html>"
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/attack3"), params, true)

        params = mutableMapOf()
        params["editor2"] =
            "Policy.getInstance(\"antisamy-slashdot.xml\");" +
            "Sammy s = new AntiSamy();" +
            "s.scan(newComment,\"\");" +
            "CleanResults();" +
            "MyCommentDAO.addComment(threadID, userID).getCleanHTML());"
        checkAssignment(webGoatUrlConfig.url("CrossSiteScripting/attack4"), params, true)

        checkResults("CrossSiteScripting")
    }
}
