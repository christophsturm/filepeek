import info.solidsoft.gradle.pitest.PitestPluginExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junit5Version = "5.7.1"
val junitPlatformVersion = "1.7.1"
val kotlinVersion = "1.4.30"

plugins {
    java
    kotlin("jvm") version "1.4.30"
    id("com.github.ben-manes.versions") version "0.36.0"
    `maven-publish`
    id("info.solidsoft.pitest") version "1.5.2"
    signing
}

group = "com.christophsturm"
version = "0.1.3"

repositories {
    //    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    testImplementation("io.strikt:strikt-core:0.29.0")
    testImplementation("com.christophsturm.failfast:failfast:0.3.0")

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
}

java {
    withJavadocJar()
    withSourcesJar()
}


publishing {
    repositories {
        maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.properties["ossrhUsername"] as String?
                password = project.properties["ossrhPassword"] as String?
            }
        }
    }


    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group as String
            artifactId = "filepeek"
            version = project.version as String
            pom {
                description.set("A library to look up kotlin source files")
                name.set("filepeek")
                url.set("https://github.com/christophsturm/filepeek")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("christophsturm")
                        name.set("Christoph Sturm")
                        email.set("me@christophsturm.com")
                    }
                }
                scm {
                    url.set("https://github.com/christophsturm/filepeek.git")
                }
            }
        }
    }
}
signing {
    sign(publishing.publications["mavenJava"])
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
