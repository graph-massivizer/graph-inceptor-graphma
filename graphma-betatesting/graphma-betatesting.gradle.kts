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
}

dependencies {
	testImplementation(libs.bundles.test)
}

dependencies {
	implementation(libs.bundles.apacheCommons)
	implementation(libs.bundles.jgraphT)
	implementation(libs.bundles.magma)
	implementation(project(":graphma-data"))
	implementation(project(":graphma-core"))
}

tasks.register<Jar>("fatJar") {
	manifest {
		attributes["Main-Class"] = "scenario_1.Main"
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	with(tasks.jar.get() as CopySpec)
}