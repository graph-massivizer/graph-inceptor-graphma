// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

plugins {
	alias(libs.plugins.buildParameters)
}

group = "graphma-build"

buildParameters {
	pluginId("graphma-build.build-parameters")
	integer("javaToolchainVersion") {
		description.set("Defines the Java toolchain version to use for compiling code")
	}
}
