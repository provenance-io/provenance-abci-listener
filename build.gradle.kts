import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application // Provide convenience executables for trying out the examples.
    jacoco
    kotlin("jvm") version "1.7.21"
    id("idea")
    id("org.gradle.crypto.checksum") version "1.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

group = "io.provenance"
version = System.getenv("VERSION") ?: "0-SNAPSHOT"

application {
    mainClass.set("io.provenance.abci.listener.ABCIListenerServerKt")
}

repositories {
    // The Google mirror is less flaky than mavenCentral()
    maven { url = project.uri("https://maven-central.storage-download.googleapis.com/maven2/") }
    maven { url = project.uri("https://packages.confluent.io/maven/") } // confluent
    maven { url = project.uri("https://jitpack.io") } // cp-testcontainers
    mavenCentral()
    mavenLocal()
}

val grpcVersion = "1.52.1"
val grpcKotlinVersion = "1.3.0"
val protobufVersion = "3.21.9"
val coroutinesVersion = "1.6.4"
val confluentVersion = "7.3.0"
val junitJupiterVersion = "5.9.2"
val testContainersVersion = "1.17.6"
val provenanceProtoKotlinVersion = "1.14.0-rc2"

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")

    // Grpc services
    implementation("io.grpc:grpc-services:$grpcVersion")
    implementation("io.provenance:proto-kotlin:$provenanceProtoKotlinVersion")

    // Grpc server
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    runtimeOnly("io.grpc:grpc-netty:$grpcVersion")

    // Log
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Kafka clients
    implementation("io.confluent:kafka-protobuf-serializer:$confluentVersion")

    // Configuration lib for JVM languages (HOCON)
    implementation("com.typesafe:config:1.4.2")

    // Test
    testImplementation(kotlin("test-junit"))
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("com.github.christophschubert:cp-testcontainers:v0.2.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<Javadoc> { enabled = false }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
        jvmTarget = "11"
        languageVersion = "1.7"
        apiVersion = "1.7"
    }
}

tasks.assembleDist {
    finalizedBy("checksumDist") // checksums are generated after assembleDist runs
}

tasks.register<Checksum>("checksumDist") {
    val dir = layout.buildDirectory.dir("distributions")
    inputFiles.setFrom(dir)
    outputDirectory.set(dir)
    checksumAlgorithm.set(Checksum.Algorithm.MD5)
    appendFileNameToChecksum.set(true)

    dependsOn(tasks.assembleDist)
}

tasks.withType<Test> {
    useJUnitPlatform()

    // JUnit 5 parallel test execution
    // https://github.com/gradle/gradle/issues/6453#issuecomment-463702749
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

    testLogging {
        showStandardStreams = true

        // set options for log level LIFECYCLE
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
        )

        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true

        // set options for log level DEBUG and INFO
        debug {
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
                org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
            )

            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat
    }

    afterSuite(
        KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) { // will match the outermost suite
                println(
                    "Results: ${result.resultType} (" +
                        "${result.testCount} tests, " +
                        "${result.successfulTestCount} successes," +
                        "${result.failedTestCount} failures, " +
                        "${result.skippedTestCount} skipped)"
                )
            }
        })
    )

    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.withType<JacocoCoverageVerification> {
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.logging.log4j" && (requested.version == "2.14.1") || (requested.version == "2.15.0")) {
            useVersion("2.15.0")
            because("CVE-2021-44228")
        }
    }
}
