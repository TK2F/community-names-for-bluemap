plugins {
    java
}

group = "com.tk2lab.bluemapcommunitynames"
version = "0.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.64-stable")
    compileOnly("de.bluecolored:bluemap-api:2.7.8")
    compileOnly(fileTree("libs") {
        include("BlueMapAPI-*.jar")
        include("bluemap-api-*.jar")
    })
    compileOnly("net.luckperms:api:5.4")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.64-stable")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}
