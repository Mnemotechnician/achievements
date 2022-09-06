import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.dokka") version "1.6.10"
	id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"
	`maven-publish`
}

dependencies {
}

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(*configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())

	from("assets/**")
	exclude("bundle*.properties")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.mnemotechnician.achievements"
			artifactId = "core"
			version = "v1.0"

			from(components["java"])
		}
	}
}

