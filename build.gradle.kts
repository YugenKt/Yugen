import org.jetbrains.dokka.gradle.DokkaTask
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