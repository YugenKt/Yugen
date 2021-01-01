import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.20"
}

group = "yugen"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:6.0.0")

    // logging for tests
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("ch.qos.logback:logback-core:1.2.3")

    // kotlin test
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // junit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
        systemProperties["logback.configurationFile"] =
            File(projectDir, "src/test/resources/logback-test.xml").absolutePath
    }

    jar {
        manifest {
            attributes("Implementation-Version" to archiveVersion)
        }
    }

    dokkaHtml {
        outputDirectory.set(buildDir.resolve("../docs"))

        dokkaSourceSets {
            named("main") {
                //moduleName.set("Yugen")
                //moduleVersion.set(version as String)

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/LewisTehMinerz/Yugen/tree/${version}/" +
                            "src/main/kotlin"
                    ))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}