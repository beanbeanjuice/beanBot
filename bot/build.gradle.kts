import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("io.github.goooler.shadow") version "8.1.7"
    id("java")
}

group = "com.beanbeanjuice"
version = "4.0.0"

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.beanbeanjuice.cafebot.CafeBot"
    }
}

allprojects {
    group = "com.beanbeanjuice"

    apply(plugin = "java")
    apply(plugin = "io.github.goooler.shadow")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()

        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }

        maven {
            name = "OSSRH-snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "OSSRH-releases"
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }

        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        // Lombok
        compileOnly("org.projectlombok", "lombok", "1.18.32")
        annotationProcessor("org.projectlombok", "lombok", "1.18.32")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        destinationDirectory.set(File(rootProject.projectDir, "libs"))

        doLast {
            archiveVersion.set(project.version as String)
            println("Compiling: " + project.name + "-" + project.version + ".jar")
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}

tasks.clean {
    doLast {
        file("libs").deleteRecursively()
    }
}

dependencies {
    implementation(project(":wrappers:cafe-api-wrapper"))

    implementation("net.dv8tion", "JDA", "5.2.1") { exclude(module = "opus-java") }

    implementation("org.apache.logging.log4j", "log4j-api", "2.24.1")  // Logging - https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j", "log4j-core", "2.24.1")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.24.1")  // JDA logging.

    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.18.1")  // JSON - https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core

    implementation("org.apache.httpcomponents.client5", "httpclient5", "5.3.1")  // HTTP Requests (For OpenAI)

    implementation("com.github.twitch4j", "twitch4j", "1.23.0")  // Twitch - https://github.com/twitch4j/twitch4j

    compileOnly("org.projectlombok", "lombok", "1.18.32")
    annotationProcessor("org.projectlombok", "lombok", "1.18.32")

    testImplementation("junit", "junit", "4.13.2")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.8.1")
}

tasks.withType<ShadowJar> {
    minimize {
        exclude(dependency("io.github.xanthic.cache:.*:.*"))
        exclude(dependency("org.apache.logging.log4j:.*:.*"))
        exclude(dependency("com.github.twitch4j:.*:.*"))
    }
}

configure<ProcessResources>("processResources") {
    filesMatching("cafebot.properties") {
        expand(project.properties)
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
