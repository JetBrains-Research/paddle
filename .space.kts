job("Paddle / Build ") {
    container("openjdk:11") {
        shellScript {
            content = """
                set -e
                ./gradlew build
            """.trimIndent()
        }
    }
}


job("Paddle / Test ") {
    container("openjdk:11") {
        shellScript {
            content = """
                set -e
                ./gradlew test
            """.trimIndent()
        }
    }
}

job("Paddle / Release / Docker") {
    startOn {
        gitPush {
            enabled = false
        }
    }

    val version = "0.4.5"

    val types = listOf("2.7", "3.7", "3.8", "3.9", "3.10").map { "paddle-py-${it.replace(".", "-")}" } + listOf("paddle")

    for (type in types) {
        docker {
            build {
                context = "."
                file = "./scripts/docker/Dockerfile"
                target = type
                args["VERSION"] = version
            }
            push(" registry.jetbrains.team/p/paddle/docker/${type}") {
                tags(version)
            }
        }
    }
}
