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

compileJava {
  options.encoding = "UTF-8"
}

java {
  withSourcesJar()
  withJavadocJar()
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.named("javadoc") {
  options.encoding = "UTF-8"
}

repositories {
  mavenCentral()
}

tasks.withType(AbstractArchiveTask) {
  preserveFileTimestamps = false
  reproducibleFileOrder = true
}

tasks.withType(GenerateModuleMetadata) {
  enabled = true
}

dependencies {
  api("io.github.jbock-java:either:1.5.2")
  testImplementation platform("org.junit:junit-bom:5.12.2")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core:5.16.1")
}

jar {
  manifest {
    attributes(
      "Implementation-Version": project.properties["version"]
    )
  }
}

tasks.named("test") {
  useJUnitPlatform()
}

// https://central.sonatype.org/pages/gradle.html
publishing {
  publications {
    mavenJava(MavenPublication) {
      artifactId = "jbock"
      from components.java

      artifact sourcesJar
      artifact javadocJar

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
      url = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
      credentials {
        username = System.getenv("OSS_USER")
        password = System.getenv("OSS_PASS")
      }
    }
  }
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
signing {
  def signingKey = findProperty("signingKey")
  def signingPassword = findProperty("signingPassword")
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign publishing.publications.mavenJava
}
