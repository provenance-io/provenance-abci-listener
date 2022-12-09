import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application // Provide convenience executables for trying out the examples.
    kotlin("jvm") version "1.7.21"
    id("idea")
    id("com.google.protobuf") version "0.8.18"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

group = "io.provenance"
version = System.getenv("VERSION") ?: "-SNAPSHOT"

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

val grpcVersion = "1.51.0"
val grpcKotlinVersion = "1.3.0"
val protobufVersion = "3.21.9"
val coroutinesVersion = "1.6.4"
val confluentVersion = "7.3.0"
val junitJupiterVersion = "5.9.1"
val testContainersVersion = "1.17.6"

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")

    // Grpc
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-services:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    // Grpc server
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    runtimeOnly("io.grpc:grpc-netty:$grpcVersion")

    // Log
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    // Kafka clients
    implementation("io.confluent:kafka-protobuf-serializer:$confluentVersion")

    // Configuration lib for JVM languages (HOCON)
    implementation("com.typesafe:config:1.4.2")

    // Test
    testImplementation(kotlin("test-junit"))
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("com.github.christophschubert:cp-testcontainers:v0.2.1")
}

// this makes it so IntelliJ picks up the sources but then ktlint complains

sourceSets {
    val main by getting { }
    main.java.srcDirs("build/generated/source/proto/main/grpc")
    main.java.srcDirs("build/generated/source/proto/main/java")
    main.java.srcDirs("build/generated/source/proto/main/grpckt")
    main.java.srcDirs("build/generated/source/proto/main/kotlin")
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
//            "-Xopt-in=kotlin.contracts.ExperimentalContracts"
        )
        jvmTarget = "11"
        languageVersion = "1.7"
        apiVersion = "1.7"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Test> {
    useJUnitPlatform()

//    maxParallelForks = Runtime.getRuntime().availableProcessors().intdiv(2) ?: 1

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
                    "Results: {} ({} tests, {} successes, {} failures, {} skipped)"
                        .format(
                            result.resultType,
                            result.testCount,
                            result.successfulTestCount,
                            result.failedTestCount,
                            result.skippedTestCount
                        )
                )
            }
        })
    )
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.logging.log4j" && (requested.version == "2.14.1") || (requested.version == "2.15.0")) {
            useVersion("2.15.0")
            because("CVE-2021-44228")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}
