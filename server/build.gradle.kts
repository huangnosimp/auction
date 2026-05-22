plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
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
    testImplementation("com.h2database:h2:2.4.240")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("com.mysql:mysql-connector-j:9.6.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation(project(":common"))
    implementation("com.cloudinary:cloudinary-http5:2.3.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "vn.io.huangnosimp.Main"
    }
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "vn.io.huangnosimp.Main"
    }
}

tasks.register<Copy>("copyJarToDeploy") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)
    into("${rootProject.projectDir}/docker")
    rename { "server.jar" }
}
tasks.shadowJar {
    finalizedBy("copyJarToDeploy")
}
