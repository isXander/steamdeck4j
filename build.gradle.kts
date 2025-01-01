plugins {
    `java-library`
    `maven-publish`
}

group = "dev.isxander"
version = "1.1.7"

repositories {
    mavenCentral()
}

dependencies {
    fun apiVer(notation: String, action: Action<MutableVersionConstraint>) = api(notation) {
        version {
            action.execute(this)
        }
    }

    apiVer("com.google.code.gson:gson") { prefer("2.11.0") }
    apiVer("org.slf4j:slf4j-api") { prefer("2.0.13") }
    implementation("org.jetbrains:annotations:24.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "steamdeck4j"
        }
    }

    repositories {
        val username = "XANDER_MAVEN_USER".let { System.getenv(it) ?: findProperty(it) }?.toString()
        val password = "XANDER_MAVEN_PASS".let { System.getenv(it) ?: findProperty(it) }?.toString()
        if (username != null && password != null) {
            maven(url = "https://maven.isxander.dev/releases") {
                name = "XanderReleases"
                credentials {
                    this.username = username
                    this.password = password
                }
            }
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}
