// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

plugins {
	`java-library`
	id("graphma-build.base-conventions")
}

// Use Maven Central for external dependencies
repositories {
	mavenCentral()
}

// Enable deprecation messages when compiling Java code
tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs.add("-Xlint:deprecation")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	maxHeapSize = "2G"
	//testLogging { events("passed") }
}