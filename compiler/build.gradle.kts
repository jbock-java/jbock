import org.gradle.api.JavaVersion;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

plugins {
  id("java")
  id("maven-publish")
  id("com.gradleup.shadow") version "8.3.6"
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

tasks.shadowJar {
  minimize()
  archiveClassifier.set("")
  relocate("io.jbock.auto.common", "io.jbock.jbock.auto.common")
  relocate("io.jbock.javapoet", "io.jbock.jbock.javapoet")
}

dependencies {
  var jbock = project(":jbock")
  var simple_component = "io.github.jbock-java:simple-component:1.024"
  var javapoet = "io.github.jbock-java:javapoet:1.15"
  implementation(javapoet)
  implementation("io.github.jbock-java:auto-common:1.2.3")
  shadow(jbock)
  compileOnly(simple_component)
  annotationProcessor("io.github.jbock-java:simple-component-compiler:1.024")
  testImplementation("io.github.jbock-java:compile-testing:0.19.12")
  testImplementation("org.mockito:mockito-core:5.16.1")
  testImplementation(platform("org.junit:junit-bom:5.12.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation(jbock)
  testImplementation(simple_component)
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
    create<MavenPublication>("shadow") {
      from(components["shadow"])
      artifactId = "jbock-compiler"

      pom {
        name = "jbock-compiler"
        packaging = "jar"
        description = "jbock annotation processor"
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
  sign(publishing.publications["shadow"])
}
