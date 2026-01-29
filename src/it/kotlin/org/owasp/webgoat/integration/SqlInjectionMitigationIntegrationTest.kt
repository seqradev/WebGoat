/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test

class SqlInjectionMitigationIntegrationTest : IntegrationTest() {
    @Test
    fun runTests() {
        startLesson("SqlInjectionMitigations")

        var params = mutableMapOf<String, Any>()
        params["field1"] = "getConnection"
        params["field2"] = "PreparedStatement prep"
        params["field3"] = "prepareStatement"
        params["field4"] = "?"
        params["field5"] = "?"
        params["field6"] = "prep.setString(1,\"\")"
        params["field7"] = "prep.setString(2,\\\"\\\")"
        checkAssignment(webGoatUrlConfig.url("SqlInjectionMitigations/attack10a"), params, true)

        params["editor"] =
            """
            try {
                Connection conn = DriverManager.getConnection(DBURL,DBUSER,DBPW);
                PreparedStatement prep = conn.prepareStatement("select id from users where name = ?");
                prep.setString(1,"me");
                prep.execute();
                System.out.println(conn);   //should output 'null'
            } catch (Exception e) {
                System.out.println("Oops. Something went wrong!");
            }
            """.trimIndent()
        checkAssignment(webGoatUrlConfig.url("SqlInjectionMitigations/attack10b"), params, true)

        params = mutableMapOf()
        params["userid_sql_only_input_validation"] = "Smith';SELECT/**/*/**/from/**/user_system_data;--"
        checkAssignment(webGoatUrlConfig.url("SqlOnlyInputValidation/attack"), params, true)

        params = mutableMapOf()
        params["userid_sql_only_input_validation_on_keywords"] =
            "Smith';SESELECTLECT/**/*/**/FRFROMOM/**/user_system_data;--"
        checkAssignment(webGoatUrlConfig.url("SqlOnlyInputValidationOnKeywords/attack"), params, true)

        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .contentType(ContentType.JSON)
            .get(
                webGoatUrlConfig.url(
                    "SqlInjectionMitigations/servers?column=(case when (true) then hostname else id end)",
                ),
            ).then()
            .statusCode(200)

        RestAssured
            .given()
            .`when`()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", webGoatCookie)
            .contentType(ContentType.JSON)
            .get(webGoatUrlConfig.url("SqlInjectionMitigations/servers?column=unknown"))
            .then()
            .statusCode(500)
            .body(
                "trace",
                containsString(
                    "select id, hostname, ip, mac, status, description from SERVERS where status <> 'out of order' order by",
                ),
            )

        params = mutableMapOf()
        params["ip"] = "104.130.219.202"
        checkAssignment(webGoatUrlConfig.url("SqlInjectionMitigations/attack12a"), params, true)

        checkResults("SqlInjectionMitigations")
    }
}
