plugins {
    application
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.runtime") version "2.0.1"
}

group = "vn.io.huangnosimp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation(project(":common"))
    implementation("com.google.code.gson:gson:2.13.2")
}

javafx {
    version = "25.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("vn.io.huangnosimp.Main")
}

runtime {
    options.set(listOf("--strip-debug", "--compress=zip-6", "--no-header-files"))

    jpackage {
        imageName = "Auction"
        appVersion = "1.0"
    }
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "vn.io.huangnosimp.Main"
    }
}