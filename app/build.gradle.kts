import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.gradle.spotless.SpotlessExtension
/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.6/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.adarshr.test-logger") version "4.0.0"
    // Spotless plugin to format code
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    // checkstyle
}



repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)

    val vertxVersion = "4.5.0"
    val mongoDriverVersion = "4.5.0"
    val slf4jVersion = "2.0.9"
    val swaggerCoreVersion  = "2.2.9"
    val gsonVersion = "2.10.1"
    val vertxSwaggerRouterVersion = "1.6.0"
    //TODO poco chiaro quale framework usiamo per i test.....
    // testImplementation("junit:junit:4.13.2") // Or 'org.junit.jupiter:junit-jupiter-api:5.10.1'
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")


    // Vert.x framework
    implementation("io.vertx:vertx-web:${vertxVersion}")
    implementation("io.vertx:vertx-core:${vertxVersion}")
    implementation("io.vertx:vertx-web-client:${vertxVersion}")
    implementation("io.vertx:vertx-web-api-contract:${vertxVersion}")
    testImplementation("io.vertx:vertx-junit5:${vertxVersion}")

    // MongoDB driver
    implementation("org.mongodb:mongodb-driver-sync:${mongoDriverVersion}")



// Non usata davvero.....
implementation("com.diffplug.spotless:spotless-lib:2.45.0")


    // SLF4J logging
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    implementation ("org.slf4j:slf4j-api:${slf4jVersion}") // SLF4J API
    implementation ("ch.qos.logback:logback-classic:1.4.11") // SLF4J binding for Logback

    // Swagger integration (optional)
    implementation ("io.swagger.core.v3:swagger-core:${swaggerCoreVersion}")
    implementation ("com.github.phiz71:vertx-swagger-router:${vertxSwaggerRouterVersion}")

    // Gson (optional)
    implementation("com.google.code.gson:gson:${gsonVersion}")
}

// tasks.register("updateGitHooks", Copy::class) {
//     from("./scripts/pre-commit")
//     into("./.git/hooks")
// }

// tasks.getByName("assemble").dependsOn(tasks.getByName("updateGitHooks"))

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
tasks.register<Test>("integrationTest", Test::class) {
    group = "verification"
    description = "Runs unit tests"
    useJUnitPlatform()
    // Configura le impostazioni del task di test
    testClassesDirs = sourceSets.getByName("test").output.classesDirs
    classpath = sourceSets.getByName("test").runtimeClasspath

    filter{
        includeTestsMatching("integration.*")
    }
}

tasks.test {
        filter {
            excludeTestsMatching("integration.*")
            // includeTestsMatching("game.*")
            }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

// tasks.withType<Jar> {
//     manifest {
//         attributes["Main-Class"] = "server.Main"
//         attributes["Class-Path"] = configurations
//         .runtimeClasspath
//         .get()
//         .joinToString(separator = " ") { file ->
//             "libs/${file.name}"
//         }
//     }

// }

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("Middleware")
    manifest {
        attributes["Main-Class"] = "server.Main"
    }
    // dependsOn(configurations.runtimeClasspath)
    // from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })
    // with(tasks.getByName<JavaCompile>("compileJava"))
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })

    // Dipendenza dal task di compilazione Java
    dependsOn("compileJava")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Puoi utilizzare altre strategie come DuplicatesStrategy.WARN per avvisare ma non fermare la build

}
// tasks.withType<ShadowJar> {
//     classifier = "fat"
//     manifest {
//       attributes["Main-Verticle"] = "server.AppServer" 
//     }
//     mergeServiceFiles()
// }

//i commented this
// application {
//     // Define the main class for the application.
//     mainClass = "server.Main"
// }

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "server.Main"
    }
}


spotless {
 java {
        importOrder() // standard import order
        removeUnusedImports()
        googleJavaFormat() // has its own section below
        eclipse()          // has its own section below
    }


}
checkstyle {
    toolVersion = "8.44" // Versione di Checkstyle
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml") // Configurazione di Checkstyle
    // showViolations = true
}


testlogger {
    theme = ThemeType.MOCHA
    showExceptions = true
    showStackTraces = true
    showFullStackTraces = false
    showCauses = true
    slowThreshold = 2000
    showSummary = true
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    showStandardStreams = false
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true
    logLevel = LogLevel.LIFECYCLE
}