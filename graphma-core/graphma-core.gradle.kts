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
	implementation(libs.bundles.jgraphT)
	implementation(libs.bundles.magma)
	implementation(project(":graphma-data"))
}