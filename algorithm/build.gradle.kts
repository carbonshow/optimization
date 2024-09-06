plugins {
    id("java")
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

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
