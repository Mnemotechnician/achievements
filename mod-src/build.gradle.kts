import org.jetbrains.kotlin.gradle.tasks.*

plugins {
	kotlin("jvm")
	`maven-publish`
	id("org.jetbrains.dokka") version "1.6.10"
}

val jarName = "achievements"

dependencies {
	implementation("com.github.mnemotechnician:mkui:-SNAPSHOT")
	implementation(project(":core"))
	implementation(project(":gui"))
}

sourceSets["main"].java.srcDir("$buildDir/gen/kotlin")

val mergeBundles by tasks.registering {
	outputs.upToDateWhen { false }
	outputs.dir(layout.buildDirectory.dir("genBundles"))

	doLast {
		val projects = project.configurations
			.flatMap { it.allDependencies }
			.filterIsInstance<ProjectDependency>()
			.map { it.dependencyProject }

		val bundles = (projects + project)
			.mapNotNull {
				it.projectDir.resolve("assets/bundles").takeIf { it.exists() }
			}
			.distinct()
			.flatMap {
				it.listFiles()?.filter { it.name.endsWith(".properties") && it.name.startsWith("bundle") } ?: listOf()
			}
			.also { if (it.isEmpty()) return@doLast }

		val bundleMap = HashMap<String, List<File>>()
		bundles.forEach {
			val kind = it.name.removeSurrounding("bundle", ".properties")

			if (kind !in bundleMap) {
				bundleMap[kind] = bundles.filter { it.name.removeSurrounding("bundle", ".properties") == kind }
			}
		}

		val outputDir = layout.buildDirectory.dir("genBundles").get().file("bundles").asFile
		outputDir.deleteRecursively()
		outputDir.mkdirs()

		bundleMap.forEach { (kind, files) ->
			val target = outputDir.resolve("bundle$kind.properties")

			target.writeText("# Generated multibundle file")
			files.forEach {
				target.appendText("\n\n${"#".repeat(40)}\n")
				target.appendText("# Included from ${it.absolutePath}:\n")
				target.appendBytes(it.readBytes())
			}
		}
	}
}

val generateIconAccessors by tasks.registering {
	val className = "ASprites"

	inputs.dir(projectDir.resolve("assets").absolutePath)
	outputs.file(layout.buildDirectory.file("gen/kotlin/$className.kt"))

	doLast {
		layout.buildDirectory.file("gen/kotlin/$className.kt").get().asFile.apply {
			writeText(buildString {
				appendLine("""
					package com.github.mnemotechnician.achievements.mod.gen

					import arc.Core
					import arc.graphics.g2d.*
					import arc.scene.style.*
					
					
					/** Generated class containing compile-time accessors for icon assets. */
					object $className {
					    fun drawable(name: String) = (Core.atlas.drawable("achievements-" + name) as TextureRegionDrawable).also {
					        if (!Core.atlas.isFound(it.region)) throw RuntimeException("Region " + name + " is not found! (Are you accessing AIcon before the end of texture packing?)")
					    }
				""".trimIndent())

				projectDir.resolve("assets/sprites/")
					.walkTopDown()
					.filter { !it.isDirectory }
					.filter { it.name.endsWith(".png") }
					.forEach { file ->
						val name = file.name.removeSuffix(".png")
						val canonicalName = buildString {
							var nextCapital = false
							name.forEach {
								if (!it.isLetter() && !it.isDigit()) {
									nextCapital = true
								} else {
									append(if (nextCapital) it.toUpperCase() else it)
									nextCapital = false
								}
							}
						}

						appendLine("""    val $canonicalName = drawable("$name")""")
					}

				append("}")
			})
		}
	}
}

tasks.withType<KotlinCompile> {
	dependsOn(generateIconAccessors)
}

tasks.jar {
	dependsOn(mergeBundles)

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveFileName.set("${jarName}-desktop.jar")

	from(rootDir) {
		include("mod.hjson")
		include("icon.png")
	}

	from("$buildDir/genBundles")
	from("assets")
	from(*configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())

}

task("jarAndroid") {
	dependsOn("jar")
	
	doLast {
		val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
		
		if(sdkRoot == null || sdkRoot.isEmpty() || !File(sdkRoot).exists()) {
			throw GradleException("""
				No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.
				Note: if the gradle daemon has been started before ANDROID_HOME env variable was defined, it won't be able to read this variable.
				In this case you have to run "./gradlew --stop" and try again
			""".trimIndent())
		}
		
		println("searching for an android sdk... ")
		val platformRoot = File("$sdkRoot/platforms/").listFiles()?.filter {
			val fi = File(it, "android.jar")
			val valid = fi.exists() && it.name.startsWith("android-")
			
			if (valid) {
				print(it)
				println(" — OK.")
			}
			return@filter valid
		}?.maxByOrNull {
			it.name.substring("android-".length).toIntOrNull() ?: -1
		}
		
		if (platformRoot == null) {
			throw GradleException("No android.jar found. Ensure that you have an Android platform installed. (platformRoot = $platformRoot)")
		} else {
			println("using ${platformRoot.absolutePath}")
		}
		
		
		//collect dependencies needed to translate java 8 bytecode code to android-compatible bytecode (yeah, android's dvm and art do be sucking)
		val dependencies = (configurations.compileClasspath.files + configurations.runtimeClasspath.files + File(platformRoot, "android.jar")).map { it.path }
		val dependenciesStr = Array<String>(dependencies.size * 2) {
			if (it % 2 == 0) "--classpath" else dependencies.elementAt(it / 2)
		}
		
		//dexing. As a result of this process, a .dex file will be added to the jar file. This requires d8 tool in your $PATH
		exec {
			workingDir("$buildDir/libs")
			commandLine("d8", *dependenciesStr, "--min-api", "14", "--output", "${jarName}-android.jar", "${jarName}-desktop.jar")
		}
	}
}

task<Jar>("release") {
	dependsOn("jarAndroid")
	
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveFileName.set("${jarName}-any-platform.jar")

	from(
		zipTree("$buildDir/libs/${jarName}-desktop.jar"),
		zipTree("$buildDir/libs/${jarName}-android.jar")
	)

	doLast {
		delete { delete("$buildDir/libs/${jarName}-desktop.jar") }
		delete { delete("$buildDir/libs/${jarName}-android.jar") }
	}
}

