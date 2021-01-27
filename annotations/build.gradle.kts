import org.gradle.plugins.signing.Sign

plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

group = "com.github.h908714124"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(15))
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"
    options.compilerArgs.addAll(listOf("--release", "8"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
    }
}

tasks.withType<AbstractArchiveTask>() {
    setPreserveFileTimestamps(false)
    setReproducibleFileOrder(true)
}

// Disable Gradle module.json as it lists wrong dependencies
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
gradle.taskGraph.whenReady {
    allTasks.filterIsInstance<Sign>().forEach { task ->
        allprojects {
            ext["signatory.keyId"] = System.getenv("SIGNING_KEY_ID")
            ext["signatory.password"] = System.getenv("SIGNING_PASSWORD")
        }
    }
}

// https://central.sonatype.org/pages/gradle.html
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jbock-annotations"

            pom {
                name.set("jbock")
                description.set("A command line parser generator ")
                url.set("https://github.com/h908714124/jbock")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("h908714124")
                        name.set("h908714124")
                        email.set("kraftdurchblumen@gmx.de")
                    }
                }
                scm {
                    connection.set("scm:svn:https://github.com/h908714124/jbock.git")
                    developerConnection.set("scm:svn:https://github.com/h908714124/jbock.git")
                    url.set("https://github.com/h908714124/jbock")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSS_USER")
                password = System.getenv("OSS_PASS")
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey")
    val signingPassword = findProperty("signingPassword")
    useInMemoryPgpKeys("" + signingKey, "" + signingPassword)
    sign(publishing.publications["mavenJava"])
}
