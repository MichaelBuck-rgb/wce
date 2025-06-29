plugins {
    id("org.beryx.jlink") version "3.1.1"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.powergem"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.avaje:avaje-jsonb:3.5")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("info.picocli:picocli:4.7.6")

    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
    annotationProcessor("io.avaje:avaje-jsonb-generator:3.5")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.11")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.compileJava {
    options.compilerArgs.add("-proc:full")
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
            buildArgs.add("-R:MaxHeapSize=256M")
            buildArgs.add("--enable-native-access=ALL-UNNAMED")
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
    applicationDefaultJvmArgs = listOf("-agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/com.powergem/wce")
}
