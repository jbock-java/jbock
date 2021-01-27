plugins {
    id("java")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(15))
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"
    options.compilerArgs.addAll(listOf("--release", "8"))
}

dependencies {
    compileOnly("com.github.h908714124:jbock-annotations:3.5")
    annotationProcessor("com.github.h908714124:jbock:3.5.005")
    annotationProcessor("com.github.h908714124:jbock-annotations:3.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.mockito:mockito-core:3.6.0") //  for mocking resourcebundle
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
    }
}
