import com.jfrog.bintray.gradle.BintrayExtension
import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit5Version = "5.7.0"
val junitPlatformVersion = "1.7.0"
val kotlinVersion = "1.4.21-2"

plugins {
    java
    kotlin("jvm") version "1.4.21-2"
    id("com.github.ben-manes.versions") version "0.36.0"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
    id("info.solidsoft.pitest") version "1.5.2"

}

group = "com.christophsturm"
version = "0.1.2"

repositories {
    //    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    testImplementation("io.strikt:strikt-core:0.28.1")
    testImplementation("com.christophsturm:failfast:0.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
    create<Jar>("sourceJar") {
        from(sourceSets.main.get().allSource)
        archiveClassifier.set("sources")
    }
}

artifacts {
    add("archives", tasks["jar"])
    add("archives", tasks["sourceJar"])
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            groupId = project.group as String
            artifactId = "filepeek"
            version = project.version as String
        }
    }
}

// BINTRAY_API_KEY= ... ./gradlew clean check publish bintrayUpload
bintray {
    user = "christophsturm"
    key = System.getenv("BINTRAY_API_KEY")
    publish = true
    setPublications("mavenJava")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "filepeek"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}

plugins.withId("info.solidsoft.pitest") {
    configure<PitestPluginExtension> {
        jvmArgs.set(listOf("-Xmx512m"))
        testPlugin.set("failfast")
        avoidCallsTo.set(setOf("kotlin.jvm.internal"))
//        mutators.set(setOf("NEW_DEFAULTS"))
        targetClasses.set(setOf("filepeek.*"))  //by default "${project.group}.*"
        targetTests.set(setOf("filepeek.*", "filepeektest.*"))
        pitestVersion.set("1.6.2")
        threads.set(System.getenv("PITEST_THREADS")?.toInt() ?: Runtime.getRuntime().availableProcessors())
        outputFormats.set(setOf("XML", "HTML"))
    }
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    val filtered =
        listOf("alpha", "beta", "rc", "cr", "m", "preview", "dev", "eap")
            .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*.*") }
    resolutionStrategy {
        componentSelection {
            all {
                if (filtered.any { it.matches(candidate.version) }) {
                    reject("Release candidate")
                }
            }
        }
        // optional parameters
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}
tasks.wrapper { distributionType = Wrapper.DistributionType.ALL }
val testMain = task("testMain", JavaExec::class) {
    main = "filepeek.AllTestsKt"
    classpath = sourceSets["test"].runtimeClasspath
}

tasks.check {
    dependsOn(testMain)
}
