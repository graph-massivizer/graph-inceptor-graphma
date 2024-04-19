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
	id("me.champeau.jmh") version "0.7.2"
}


dependencies {
	testImplementation(libs.bundles.test)
}

dependencies {
	implementation(libs.bundles.jgraphT)
	implementation(libs.bundles.magma)
	implementation(libs.bundles.jmh)
	implementation(project(":graphma-data"))
	implementation(project(":graphma-core"))

	annotationProcessor(libs.bundles.jmh)
}