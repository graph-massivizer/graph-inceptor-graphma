// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
	id("graphma-build.build-parameters")
}

project.pluginManager.withPlugin("java") {
	val defaultLanguageVersion = JavaLanguageVersion.of(17)
	val javaLanguageVersion = buildParameters.javaToolchainVersion.map { JavaLanguageVersion.of(it) }.getOrElse(defaultLanguageVersion)

	val extension = the<JavaPluginExtension>()
	val javaToolchainService = the<JavaToolchainService>()

	extension.toolchain.languageVersion.set(javaLanguageVersion)

	pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
		configure<KotlinJvmProjectExtension> {
			jvmToolchain {
				languageVersion.set(javaLanguageVersion)
			}
		}
	}

	tasks.withType<JavaExec>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor(extension.toolchain))
	}

	tasks.withType<JavaCompile>().configureEach {
		outputs.cacheIf { javaLanguageVersion == defaultLanguageVersion }
	}
}
