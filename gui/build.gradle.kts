import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	`maven-publish`
	id("org.jetbrains.dokka") version "1.6.10"
}

dependencies {
	implementation(project(":core"))
}

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(*configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())

	from("assets")
	exclude("bundle*.properties")
}
