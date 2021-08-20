group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation("com.github.fracpete", "rsync4j-all", "3.2.3-3")
}
