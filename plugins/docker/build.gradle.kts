group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation("com.github.docker-java", "docker-java-transport-httpclient5", "3.2.11")
    implementation("com.github.docker-java", "docker-java-core", "3.2.11")
}
