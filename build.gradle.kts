plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.50")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
    idea
    application
}

application {
    mainClassName = "com.github.viktornordling.kvitter.Kvitter"
}

repositories {
    jcenter()
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8")
    implementation(group = "com.charleskorn.kaml", name = "kaml", version = "0.12.0")
    implementation(group = "com.github.ajalt", name = "clikt", version = "2.1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    compile(group = "org.twitter4j", name = "twitter4j-core", version = "4.0.6")
    compile(group = "joda-time", name = "joda-time", version = "2.3")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.4.1")
}
