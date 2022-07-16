import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.0"
	`maven-publish`
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	compileOnly(kotlin("stdlib-jdk8"))
	
	compileOnly("com.github.Anuken.Arc:arc-core:master-da27a54ef9-1") // 2022.06.27.
	compileOnly("com.github.Anuken:MindustryJitpack:d380051459")

	implementation("com.github.mnemotechnician:mkui:v1.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xuse-k2"
	}
}


tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(*configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())

	from("assets/**")
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

