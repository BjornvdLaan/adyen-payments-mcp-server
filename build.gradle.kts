plugins {
    java
    application
}

group = "nl.bjornvanderlaan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "nl.bjornvanderlaan.adyenpayments.mcpserver.AdyenMcpServer"
    }

    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    // MCP
    implementation(platform("io.modelcontextprotocol.sdk:mcp-bom:0.9.0"))
    implementation("io.modelcontextprotocol.sdk:mcp")

    // Adyen
    implementation("com.adyen:adyen-java-api-library:36.0.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}