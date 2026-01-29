/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration

import org.junit.jupiter.api.Test

class SqlInjectionLessonIntegrationTest : IntegrationTest() {
    companion object {
        const val SQL_2 = "select department from employees where last_name='Franco'"
        const val SQL_3 = "update employees set department='Sales' where last_name='Barnett'"
        const val SQL_4_DROP = "alter table employees drop column phone"
        const val SQL_4_ADD = "alter table employees add column phone varchar(20)"
        const val SQL_5 = "grant select on grant_rights to unauthorized_user"
        const val SQL_9_ACCOUNT = " ' "
        const val SQL_9_OPERATOR = "or"
        const val SQL_9_INJECTION = "'1'='1"
        const val SQL_10_LOGIN_COUNT = "2"
        const val SQL_10_USERID = "1 or 1=1"
        const val SQL_11_A = "Smith' or '1' = '1"
        const val SQL_11_B = "3SL99A'  or '1'='1"
        const val SQL_12_A = "Smith"
        const val SQL_12_B = "3SL99A' ; update employees set salary= '100000' where last_name='Smith"
        const val SQL_13 = "%update% '; drop table access_log ; --'"
    }

    @Test
    fun runTests() {
        startLesson("SqlInjection")

        var params = mutableMapOf<String, Any>()
        params["query"] = SQL_2
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack2"), params, true)

        params = mutableMapOf()
        params["query"] = SQL_3
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack3"), params, true)

        params = mutableMapOf()
        params["query"] = SQL_4_ADD
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack4"), params, true)

        params = mutableMapOf()
        params["query"] = SQL_5
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack5"), params, true)

        params = mutableMapOf()
        params["operator"] = SQL_9_OPERATOR
        params["account"] = SQL_9_ACCOUNT
        params["injection"] = SQL_9_INJECTION
        checkAssignment(webGoatUrlConfig.url("SqlInjection/assignment5a"), params, true)

        params = mutableMapOf()
        params["login_count"] = SQL_10_LOGIN_COUNT
        params["userid"] = SQL_10_USERID
        checkAssignment(webGoatUrlConfig.url("SqlInjection/assignment5b"), params, true)

        params = mutableMapOf()
        params["name"] = SQL_11_A
        params["auth_tan"] = SQL_11_B
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack8"), params, true)

        params = mutableMapOf()
        params["name"] = SQL_12_A
        params["auth_tan"] = SQL_12_B
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack9"), params, true)

        params = mutableMapOf()
        params["action_string"] = SQL_13
        checkAssignment(webGoatUrlConfig.url("SqlInjection/attack10"), params, true)

        checkResults("SqlInjection")
    }
}
