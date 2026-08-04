import org.gradle.api.JavaVersion;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;

plugins {
  id("java-library")
  id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.jbock-java"

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
  (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
}

repositories {
  mavenCentral()
}

dependencies {
  api("io.github.jbock-java:either:1.5.2")
  testImplementation(platform("org.junit:junit-bom:6.1.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Version"] = project.version
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
  coordinates("io.github.jbock-java", "jbock", project.version?.toString())

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
  publishToMavenCentral()
  signAllPublications()
}
