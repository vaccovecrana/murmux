plugins { id("io.vacco.oss.gitflow") version "1.5.3" }

group = "io.vacco.murmux"
version = "2.3.2"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  sharedLibrary(true, false)
}

val api by configurations

dependencies {
  api("org.slf4j:slf4j-api:2.0.16")
  testImplementation("com.github.mizosoft.methanol:methanol:1.7.0")
  testImplementation("io.vacco.shax:shax:2.0.16.0.4.3")
  testImplementation("org.slf4j:jul-to-slf4j:2.0.16")
}
