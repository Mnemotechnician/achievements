import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.0"
	id("org.jetbrains.dokka") version "1.6.10"
	`maven-publish`
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	compileOnly(kotlin("stdlib-jdk8"))
	
	compileOnly("com.github.Anuken.Arc:arc-core:v136")
	compileOnly("com.github.Anuken:MindustryJitpack:v136")

	implementation("com.github.mnemotechnician:mkui:-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += arrayOf(
			"-Xuse-k2",
			"-Xcontext-receivers"
		)
	}
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

