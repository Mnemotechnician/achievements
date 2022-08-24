import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.20-Beta"
}

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")

	repositories {
		mavenCentral()
		mavenLocal()
		maven("https://jitpack.io")
	}

	dependencies {
		implementation(kotlin("stdlib-jdk8"))
		compileOnly("com.github.Anuken.Arc:arc-core:v137")
		compileOnly("com.github.Anuken:MindustryJitpack:74a0321db8")
		implementation("com.github.mnemotechnician:mkui:-SNAPSHOT")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "1.8"
			freeCompilerArgs += arrayOf(
				"-Xuse-k2",
				"-Xcontext-receivers"
			)
		}
	}
}
