plugins {
  id "java"
}

repositories {
  mavenCentral()
}

compileJava {
  options.encoding = "UTF-8"
}

java {
  sourceCompatibility = "17"
  targetCompatibility = "17"
}

dependencies {
  def jbock = project(":jbock")
  implementation(jbock)
  annotationProcessor project(":compiler")
  annotationProcessor project(":jbock")
  testImplementation platform("org.junit:junit-bom:5.12.2")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named("test") {
  useJUnitPlatform()
}
