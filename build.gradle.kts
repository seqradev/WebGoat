import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    jacoco
}

group = "org.owasp.webgoat"
version = "2025.4-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    jvmToolchain(21) // Kotlin 2.1 max target is JVM_21, use separate toolchain
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
        )
    }
}

// Configure all-open plugin for JPA entities (needed for lazy loading)
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// Dependency versions
val asciidoctorjVersion = "3.0.0"
val bootstrapVersion = "5.3.5"
val cglibVersion = "3.3.0"
val commonsCollectionsVersion = "3.2.1"
val commonsCompressVersion = "1.28.0"
val commonsIoVersion = "2.20.0"
val commonsLang3Version = "3.14.0"
val commonsTextVersion = "1.14.0"
val guavaVersion = "33.5.0-jre"
val jaxbVersion = "2.3.1"
val jjwtVersion = "0.9.1"
val jose4jVersion = "0.9.3"
val jqueryVersion = "3.7.1"
val jsoupVersion = "1.19.1"
val webjarsLocatorCoreVersion = "0.59"
val wiremockVersion = "3.13.1"
val xmlResolverVersion = "1.2"
val xstreamVersion = "1.4.5"
val zxcvbnVersion = "1.9.0"
val playwrightVersion = "1.55.0"
val commonsExecVersion = "1.5.0"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")

    // Thymeleaf extras
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Database
    implementation("org.hsqldb:hsqldb")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-hsqldb")

    // AsciiDoc
    implementation("org.asciidoctor:asciidoctorj:$asciidoctorjVersion")

    // Servlet API
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    // XML/JAXB
    implementation("javax.xml.bind:jaxb-api:$jaxbVersion")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    runtimeOnly("com.sun.xml.bind:jaxb-impl")

    // Security/JWT libraries
    implementation("io.jsonwebtoken:jjwt:$jjwtVersion")
    implementation("org.bitbucket.b_c:jose4j:$jose4jVersion")
    implementation("com.auth0:jwks-rsa:0.23.0")
    implementation("com.auth0:java-jwt:4.5.0")

    // Utility libraries
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("org.apache.commons:commons-exec:$commonsExecVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Intentionally vulnerable libraries (DO NOT UPDATE - needed for lessons)
    implementation("com.thoughtworks.xstream:xstream:$xstreamVersion")
    implementation("cglib:cglib-nodep:$cglibVersion")
    implementation("xml-resolver:xml-resolver:$xmlResolverVersion")
    implementation("com.nulab-inc:zxcvbn:$zxcvbnVersion")

    // WebJars
    implementation("org.webjars:bootstrap:$bootstrapVersion")
    implementation("org.webjars:jquery:$jqueryVersion")
    implementation("org.webjars:webjars-locator-core:$webjarsLocatorCoreVersion")

    // WireMock (runtime for some lessons)
    implementation("org.wiremock:wiremock-standalone:$wiremockVersion")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("com.microsoft.playwright:playwright:$playwrightVersion")
    testImplementation("com.github.terma:javaniotcpproxy:1.6")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Configure source sets for Kotlin
sourceSets {
    main {
        kotlin {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        kotlin {
            srcDirs("src/test/kotlin")
        }
    }
    // Integration tests source set
    create("integrationTest") {
        kotlin {
            srcDir("src/it/kotlin")
        }
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

// Integration test configuration
val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters"))
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // JVM arguments needed for vulnerable components lesson
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens",
        "java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.io=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.text=ALL-UNNAMED",
        "--add-opens",
        "java.desktop/java.awt.font=ALL-UNNAMED",
    )

    // Exclude integration tests from normal test task
    exclude("**/*IntegrationTest.class")
    exclude("**/*UITest.class")

    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Integration test task
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()

    jvmArgs("-Xmx512m")

    environment(
        "WEBGOAT_SSLENABLED" to "false",
        "WEBGOAT_HOST" to "127.0.0.1",
        "WEBGOAT_PORT" to "8080",
        "WEBGOAT_CONTEXT" to "/WebGoat",
        "WEBWOLF_HOST" to "127.0.0.1",
        "WEBWOLF_PORT" to "9090",
        "WEBWOLF_CONTEXT" to "/WebWolf",
    )

    shouldRunAfter(tasks.test)
}

// Spring Boot configuration
springBoot {
    mainClass.set("org.owasp.webgoat.server.StartWebGoatKt")
}

tasks.bootJar {
    archiveBaseName.set("webgoat")

    // AsciidoctorJ needs to be unpacked for proper operation
    requiresUnpack("org.asciidoctor:asciidoctorj")

    // Handle duplicate entries (e.g., jaxb-core from different transitive dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    enabled = false
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    parallel = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(false)
    }
}

// ktlint configuration
ktlint {
    version.set("1.5.0")
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}

// JaCoCo configuration
jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/lessons/**", // Lessons are intentionally vulnerable, skip coverage
                        "**/webwolf/**", // WebWolf has separate coverage requirements
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            limit {
                minimum = "0.60".toBigDecimal()
            }
        }
    }

    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
}
