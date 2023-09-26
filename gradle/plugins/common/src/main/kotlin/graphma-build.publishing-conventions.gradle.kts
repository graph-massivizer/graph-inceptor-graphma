// ╭────────────╮─────╭──╭───────╭────────────╮─────╭──╮
// │  ┬─╮  ┬─╮  │  ┬──╮  │  ┬─╮  ╮  ┬─╮  ┬─╮  │  ┬──╮  │
// ╰──╯ ╰──╯ ╰──╯─────╰──┌──╮─╯  │──╯ ╰──╯ ╰──┴─────╰──┴
//                       ╰───────╯
//   Copyright (C) Tobias Herb - All Rights Reserved.
//  Unauthorized copying of this file, via any medium
// is strictly prohibited. Proprietary and confidential.

plugins {
	`maven-publish`
	signing
	id("graphma-build.base-conventions")
	id("graphma-build.build-parameters")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			groupId    = "graphma"
			artifactId = "graphma-core"
			version    = "0.0.1"

			pom {
				name.set("graphma")
				description.set("Generic functional core library for Java")
				url.set("https://github.com/graph-massivizer/graph-inceptor")
				//licenses {
				//	license {
				//		name.set("The Apache License, Version 2.0")
				//		url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
				//	}
				//}
				developers {
					developer {
						id.set("DanielThiloSchroeder")
						name.set("Daniel Thilo Schroeder")
						email.set("contact@danielthiloschroeder.org")
					}
					developer {
						id.set("TobiasHerb")
						name.set("Tobias Herb")
						email.set("tobias.herb@esentri.com")
					}
				}
/*				scm {
					connection = 'scm:git:git://github.com:TobiasHerb/magma.git'
					developerConnection = 'scm:git:ssh://github.com/TobiasHerb/magma'
					url = 'https://github.com:TobiasHerb/magma.git'
				}*/
			}
		}
	}
}

signing {
	sign(publishing.publications["mavenJava"])
}
