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
    id("me.champeau.jmh") version "0.7.2"
}

description = "graphma-benchmarks"

dependencies {
    implementation(project(":graphma-core"))
    implementation(libs.bundles.jgraphT)
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    warmupIterations.set(2)
    iterations.set(5)
    fork.set(1)
    resultFormat.set("JSON")
    resultsFile.set(file("${project.layout.buildDirectory.get()}/reports/jmh/results.json"))
}

// Task to run the streaming ingestion benchmark
tasks.register<JavaExec>("runStreamingBenchmark") {
    group = "benchmark"
    description = "Run the streaming ingestion benchmark (KPI-1.2)"
    classpath = sourceSets["jmh"].runtimeClasspath
    mainClass.set("graphma.benchmarks.StreamingIngestionBenchmark")
}

