import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("6.1.0")
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

tasks.withType<JavaCompile> {
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

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.named<ShadowJar>("shadowJar").configure {
    minimize()
    dependencies {
        exclude(dependency("com.github.h908714124:jbock-annotations:.*"))
        exclude(dependency("javax.annotation:jsr250-api:.*"))
    }
    archiveClassifier.set("")
    relocate("dagger", "net.jbock.dagger")
    relocate("com.squareup.javapoet", "net.jbock.javapoet")
    relocate("com.google", "net.jbock.google")
    relocate("org.checkerframework", "net.jbock.org.checkerframework")
    relocate("javax.inject", "net.jbock.javax.inject")
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.google.auto:auto-common:0.11")
    implementation("com.google.guava:guava:30.1-jre")
    shadow("com.github.h908714124:jbock-annotations:3.5")
    implementation("com.google.dagger:dagger:2.30.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.30.1")
    implementation("javax.annotation:jsr250-api:1.0")
    testImplementation("com.github.h908714124:jbock-annotations:3.5")
    testImplementation("com.google.testing.compile:compile-testing:0.19")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.mockito:mockito-core:3.6.0")
}
tasks.withType<Jar> {
    manifest {
        attributes["Automatic-Module-Name"] = "net.jbock.compiler"
        attributes["Implementation-Version"] = project.version.toString()
    }
}

// Shadow ALL dependencies:
tasks.create<ConfigureShadowRelocation>("relocateShadowJar") {
    target = tasks["shadowJar"] as ShadowJar
}

// Disabling default jar task as jar is output by shadowJar
tasks.named("jar").configure {
    enabled = false
}

// Disable Gradle module.json as it lists wrong dependencies
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
gradle.taskGraph.whenReady {
    if (allTasks.filterIsInstance<Sign>().isNotEmpty()) {
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
            artifactId = "jbock"

            pom {
                name.set("jbock")
                description.set("A command line parser generator")
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

// Remove dependencies from POM: uber jar has no dependencies
configure<PublishingExtension> {
    publications {
        withType(MavenPublication::class.java) {
            if (name == "pluginMaven") {
                pom.withXml {
                    val pomNode = asNode()

                    val dependencyNodes: NodeList = pomNode.get("dependencies") as NodeList
                    dependencyNodes.forEach {
                        val node = it as Node
                        node.parent().remove(it)
                    }
                }
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
