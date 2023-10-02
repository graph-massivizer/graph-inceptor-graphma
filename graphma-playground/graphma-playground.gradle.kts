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
	testImplementation(libs.bundles.testDependencies)
	implementation(libs.bundles.magmaDependencies)
	implementation(libs.bundles.jgraphT)
	implementation(libs.bundles.arrowDependencies)
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