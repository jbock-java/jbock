import org.gradle.api.JavaVersion;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;

plugins {
  id("java-library")
  id("maven-publish")
  id("signing")
}

group = "io.github.jbock-java"

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

// https://stackoverflow.com/questions/21904269/configure-gradle-to-publish-sources-and-javadoc
java {
  withSourcesJar()
  withJavadocJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
}

repositories {
  mavenCentral()
}

tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

tasks.withType<GenerateModuleMetadata>().configureEach {
  enabled = true
}

dependencies {
  api("io.github.jbock-java:either:1.5.2")
  testImplementation("org.mockito:mockito-core:5.16.1")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core:5.16.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Version"] = project.properties["version"]
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

// https://central.sonatype.org/pages/gradle.html
publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])

      artifactId = "jbock"

      pom {
        name = "jbock"
        packaging = "jar"
        description = "jbock annotations and utils"
        url = "https://github.com/jbock-java/jbock"

        licenses {
          license {
            name = "MIT License"
            url = "https://opensource.org/licenses/MIT"
          }
        }
        developers {
          developer {
            id = "Various"
            name = "Various"
            email = "jbock-java@gmx.de"
          }
        }
        scm {
          connection = "scm:git:https://github.com/jbock-java/jbock.git"
          developerConnection = "scm:git:https://github.com/jbock-java/jbock.git"
          url = "https://github.com/jbock-java/jbock"
        }
      }
    }
  }
  repositories {
    maven {
      url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = System.getenv("OSS_USER")
        password = System.getenv("OSS_PASS")
      }
    }
  }
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["mavenJava"])
}
