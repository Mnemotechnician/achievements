import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.0"
}

val jarName = "achievements"

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	
	compileOnly("com.github.Anuken.Arc:arc-core:v136")
	compileOnly("com.github.Anuken:MindustryJitpack:v136")

	implementation("com.github.mnemotechnician:mkui:-SNAPSHOT")
	implementation(project(":core"))
	implementation(project(":gui"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += arrayOf(
			"-Xuse-k2",
			"-Xcontext-receivers"
		)
	}
}

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
				it.listFiles().filter { it.name.endsWith(".properties") && it.name.startsWith("bundle") }
			}
			.also { if (it.isEmpty()) return@doLast }

		val bundleMap = HashMap<String, List<File>>()
		bundles.forEach {
			val kind = it.name.removeSurrounding("bundle", ".properties")

			if (kind !in bundleMap) {
				bundleMap[kind] = bundles.filter { it.name.removeSurrounding("bundle", ".properties") == kind }
			}
		}

		val outputDir = File("$buildDir/genBundles/bundles")
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

