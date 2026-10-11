plugins {
  id("java")
  id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.jbock-java"

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
  (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
}

repositories {
  mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  var jbock = "io.github.jbock-java:jbock:6.0"
  var sc = "io.github.jbock-java:simple-component:2.0"
  var mockito = "org.mockito:mockito-core:5.24.0"
  implementation("io.github.jbock-java:either:1.5.3")
  implementation("com.palantir.javapoet:javapoet:0.18.0")
  implementation("io.github.jbock-java:auto-common:1.2.3")
  implementation(jbock)
  compileOnly(sc)
  annotationProcessor("io.github.jbock-java:simple-component-compiler:2.0.2")
  testImplementation("io.github.jbock-java:compile-testing:0.19.12")
  testImplementation(mockito)
  testImplementation(platform("org.junit:junit-bom:6.1.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation(jbock)
  mockitoAgent(mockito) { isTransitive = false }
}

tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Version"] = project.version
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
  jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
  coordinates("io.github.jbock-java", "jbock-compiler", project.version?.toString())
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
