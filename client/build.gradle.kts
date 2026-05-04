plugins {
    application
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "4.0.0"
}

group = "vn.io.huangnosimp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.google.code.gson:gson:2.13.2")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("vn.io.huangnosimp.Main")
}

jlink {
    options.set(listOf("--strip-debug", "--compress=zip-6", "--no-header-files", "--no-man-pages"))

    jpackage {
        val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
        val currentOs = System.getProperty("os.name")
        imageName = "AuctionClient"
        appVersion = "1.0.$buildNumber"
        installerName = "Auction"
        if (currentOs == "Windows") {
            installerOptions.addAll(listOf("--win-console"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    dependsOn(":common:jar")
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "vn.io.huangnosimp.Main"
    }
}
