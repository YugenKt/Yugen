import org.jetbrains.dokka.gradle.DokkaTask
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
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.beust:klaxon:5.0.1")
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes("Implementation-Version" to version)
    }
}

tasks.withType<DokkaTask>().configureEach {
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