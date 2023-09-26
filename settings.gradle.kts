// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

@file:Suppress("UnstableApiUsage")

import buildparameters.BuildParametersExtension

pluginManagement {
	includeBuild("gradle/plugins")
	repositories {
		gradlePluginPortal()
	}
}

plugins {
	id("graphma-build.build-parameters")
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots") {
			mavenContent {
				snapshotsOnly()
			}
		}
	}
}

val buildParameters = the<BuildParametersExtension>()

rootProject.name = "graphma"

include("graphma-core")
include("graphma-playground")
include("graphma-adapter")

// check that every subproject has a custom build file  based on the project name
rootProject.children.forEach { project ->
	project.buildFileName = "${project.name}.gradle.kts"
	require(project.buildFile.isFile) {
		"${project.buildFile} must exist"
	}
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
