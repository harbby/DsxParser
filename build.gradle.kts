plugins {
    id("java")
    id("antlr")
    signing
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.harbby"
version = "1.0.2-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    mavenCentral()
}

object versions {
    val gadtry = "1.10.7"
    val junit = "5.10.0"
    val antlr4 = "4.13.1"
    val slf4j = "2.0.13"
    val log4j2 = "2.23.1"
}

dependencies {
    antlr("org.antlr:antlr4:${versions.antlr4}")
    implementation("com.github.harbby:gadtry:${versions.gadtry}")
    implementation("org.slf4j:slf4j-api:${versions.slf4j}")

    testImplementation(platform("org.junit:junit-bom:${versions.junit}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:${versions.log4j2}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

configurations {
    create("shadowImplementation") {
        isCanBeResolved = true
        isCanBeConsumed = false
        extendsFrom(configurations.implementation.get())
    }
}
tasks.shadowJar {
    archiveBaseName.set(project.name)
    archiveClassifier.set("shaded")
    version = project.version

    //configurations = listOf(project.configurations.implementation.get())
    configurations = listOf(project.configurations.getByName("shadowImplementation"))

    dependencies {
        exclude(dependency("junit:junit:"))
        exclude(dependency("com.github.harbby:gadtry:"))
        exclude(dependency("org.slf4j:slf4j-api:"))
    }

    //relocate 'com.google.protobuf', 'shaded.com.google.protobuf'
    relocate("org.antlr", "antlr4.org.antlr")
    relocate("org.stringtemplate", "antlr4.org.stringtemplate")
    relocate("org.objectweb", "antlr4.org.objectweb")
    relocate("org.abego.treelayout", "antlr4.org.abego.treelayout")
    relocate("com.ibm.icu", "antlr4.com.ibm.icu")
}

tasks.assemble {
    dependsOn(tasks.generateGrammarSource, tasks.shadowJar)
}

tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.orNull?.allSource)
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.orNull?.destinationDir)
    tasks.javadoc.orNull?.isFailOnError = false
}
//--- gradle clean build publish
// export https_proxy=http://127.0.0.1:prot
// export http_proxy=http://127.0.0.1:prot
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(project.components.getByName("java"))
            //artifact jar
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))

            version = "${project.version}"
            artifactId = project.name
            groupId = "${project.group}"

            pom {
                name.set("dsx-parser")
                description.set("dsx parser")
                url.set("https://github.com/harbby/DsxParser")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("harbby")
                        name.set("harbby")
                        email.set("yezhixinghai@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/harbby/DsxParser")
                    connection.set("https://github.com/harbby/DsxParser.git")
                    developerConnection.set("https://github.com/harbby/DsxParser.git")
                }
            }
        }
    }

    repositories {
        maven {
            credentials {
                username = project.findProperty("mavenUsername")?.toString()
                password = project.findProperty("mavenPassword")?.toString()
            }
            // change URLs to point to your repos, e.g. http://my.org/repo
            val repository_url = if (project.version.toString().endsWith("-SNAPSHOT"))
                "https://oss.sonatype.org/content/repositories/snapshots" else
                "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            url = uri(repository_url)
        }
        mavenLocal()
    }

    signing {
        isRequired = project.hasProperty("mavenUsername")
        sign(publishing.publications.getByName("mavenJava"))
    }

    tasks.register("install") {
        dependsOn(tasks.publishToMavenLocal)
    }
    tasks.register("upload") {
        dependsOn(tasks.publish)
    }
}