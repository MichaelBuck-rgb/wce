plugins {
    id("org.beryx.jlink") version "3.1.1"
    id("org.graalvm.buildtools.native") version "0.10.4"
}

group = "com.powergem"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("info.picocli:picocli:4.7.6")

    annotationProcessor("info.picocli:picocli-codegen:4.7.6")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.11")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(21)
//        vendor = JvmVendorSpec.GRAAL_VM
//    }
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    mainClass = "com.powergem.wce.Main"
    mainModule = "com.powergem.wce.main"
}

jlink {

}

graalvmNative {
//    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("wce")
            quickBuild.set(true)
            buildArgs.add("--strict-image-heap")
            buildArgs.add("-R:MaxHeapSize=128M")
        }
        binaries.all {
            resources.autodetect()
        }
    }
    metadataRepository {
        enabled.set(true)
    }
}

application {
    applicationDefaultJvmArgs = listOf("-agentlib:native-image-agent=config-output-dir=./deleteme")
}
