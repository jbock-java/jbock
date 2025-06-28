plugins {
  id("java")
}

repositories {
  mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  var jbock = project(":jbock")
  implementation(jbock)
  annotationProcessor(project(":compiler"))
  annotationProcessor(project(":jbock"))
  testImplementation(platform("org.junit:junit-bom:5.12.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}
