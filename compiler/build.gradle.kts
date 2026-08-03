import org.gradle.api.JavaVersion;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

plugins {
  id("java")
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

tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

tasks.withType<GenerateModuleMetadata>().configureEach {
  enabled = true
}

dependencies {
  var jbock = project(":jbock")
  var simple_component = "io.github.jbock-java:simple-component:1.024"
  var javapoet = "io.github.jbock-java:javapoet:1.15"
  implementation(javapoet)
  implementation("io.github.jbock-java:auto-common:1.2.3")
  implementation(jbock)
  compileOnly(simple_component)
  annotationProcessor("io.github.jbock-java:simple-component-compiler:1.024")
  testImplementation("io.github.jbock-java:compile-testing:0.19.12")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testImplementation(platform("org.junit:junit-bom:6.1.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation(jbock)
  testImplementation(simple_component)
}

tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Version"] = project.version
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
    }
  }
  repositories {
    maven {
      url = uri(System.getenv("PUBLISH_URL"))
      credentials {
        username = System.getenv("OSS_USER")
        password = System.getenv("OSS_PASS")
      }
    }
  }
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
signing {
  val signingKey = project.findProperty("signingKey") as String?
  val signingPassword = project.findProperty("signingPassword") as String?
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["mavenJava"])
}
