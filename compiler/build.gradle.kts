plugins {
  id("java")
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
  var jbock = project(":jbock")
  var simple_component = "io.github.jbock-java:simple-component:1.024"
  implementation("io.github.jbock-java:either:1.5.3")
  implementation("com.palantir.javapoet:javapoet:0.18.0")
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
