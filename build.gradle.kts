plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.0.1-jre")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.+")

    implementation("org.slf4j:slf4j-api:1.7.+")

    testImplementation("com.google.truth:truth:1.+")

    testImplementation(platform("org.junit:junit-bom:5.+"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
