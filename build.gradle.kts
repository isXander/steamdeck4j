plugins {
    `java-library`
}

group = "dev.isxander"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains:annotations:24.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
