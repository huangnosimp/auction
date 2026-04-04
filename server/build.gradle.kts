plugins {
    id("java")
}

group = "vn.io.huangnosimp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.awaitility:awaitility:4.3.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}