// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

val expectedJavaVersion = JavaVersion.VERSION_17
val actualJavaVersion   = JavaVersion.VERSION_17
require(actualJavaVersion == expectedJavaVersion) {
	"The Magma build must be executed with Java ${expectedJavaVersion.majorVersion}. Currently executing with Java ${actualJavaVersion.majorVersion}."
}

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
}

rootProject.name = "plugins"

include("build-parameters")
include("common")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
