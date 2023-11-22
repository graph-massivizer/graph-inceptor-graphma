// 		╭━━━╮	    ╭╮ ╭━╮╭━╮
// 		┃╭━╮┃	    ┃┃ ┃┃╰╯┃┃
// 		┃┃╱╰╋━┳━━┳━━┫╰━┫╭╮╭╮┣━━╮
// 		┃┃╭━┫╭┫╭╮┃╭╮┃╭╮┃┃┃┃┃┃╭╮┃
// 		┃╰┻━┃┃┃╭╮┃╰╯┃┃┃┃┃┃┃┃┃╭╮┃
// 		╰━━━┻╯╰╯╰┫╭━┻╯╰┻╯╰╯╰┻╯╰╯
// 				 ┃┃
//
plugins {
	id("graphma-build.java-library-conventions")
	id("graphma-build.publishing-conventions")
	java
}

tasks.register<Jar>("fatJar") {
	manifest {
		attributes["Main-Class"] = "playground.examples.Main"
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	with(tasks.jar.get() as CopySpec)
}

dependencies {
	testImplementation(libs.bundles.testDependencies)
	implementation(libs.bundles.magmaDependencies)
	implementation(libs.bundles.jgraphT)
	implementation(libs.bundles.arrowDependencies)
	implementation(libs.bundles.apacheCommons)
//	implementation(libs.arrow.dataset)
//	implementation(libs.arrow.vector)
//	implementation(libs.arrow.memory)
//	runtimeOnly(libs.arrow.memory.netty)

//	implementation("org.apache.arrow:arrow-memory:12.0.1")
//	implementation("org.apache.arrow:arrow-vector:12.0.1")
//	implementation("org.apache.arrow:arrow-dataset:12.0.1")
//	runtimeOnly("org.apache.arrow:arrow-memory-netty:12.0.1")
	implementation(project(":graphma-data"))
}