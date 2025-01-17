plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.2"

}

group = "dev.carbonshow.algorithm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-math4-core:4.0-beta1")
    implementation("org.apache.commons:commons-math4-legacy:4.0-beta1")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.google.ortools:ortools-java:9.10.4067")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmh("org.openjdk.jmh:jmh-generator-bytecode:1.37")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

jmh {
    includes = listOf("IntegerPartitionBenchmark") // include pattern (regular expression) for benchmarks to be executed
    iterations = 5 // Number of measurement iterations to do.
}

tasks.register<JavaExec>("integer-partition") {
    mainClass = "dev.carbonshow.algorithm.IntegerPartition"
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("linear-programming") {
    mainClass = "dev.carbonshow.algorithm.LinearProgramming"
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("integer-linear-programming") {
  mainClass = "dev.carbonshow.algorithm.IntegerLinearProgramming"
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("max-grouping") {
  mainClass = "dev.carbonshow.algorithm.MaxGrouping"
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:-requires-transitive-automatic")
        options.compilerArgs.add("-Xlint:all")
    }
}
