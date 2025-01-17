plugins {
  id("java")
}

group = "dev.carbonshow.matchmaking"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
  implementation("ch.qos.logback:logback-classic:1.5.6")
  implementation("org.apache.commons:commons-math3:3.6.1")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.17.2")
  implementation("org.jgrapht:jgrapht-core:1.5.2")
  implementation("com.h2database:h2:2.3.232")
  implementation("org.orbisgis:h2gis:2.2.3")
  implementation("com.google.ortools:ortools-java:9.10.4067")
  implementation("org.jdbi:jdbi3-core:3.45.4")
  implementation("org.jdbi:jdbi3-sqlobject:3.45.4")
  implementation(project(":algorithm"))


  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<JavaExec>("matchmaker") {
  mainClass = "dev.carbonshow.matchmaking.MatchMaker"
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
