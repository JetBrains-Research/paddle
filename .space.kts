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

    val version = "0.4.7"

    val types = listOf("3.10, 3.9, 3.8, 2.7").map { "paddle-py-${it.replace(".", "-")}" }

    for (type in types) {
        kaniko {
            build {
                context = "."
                dockerfile = "./scripts/docker/Dockerfile"
                target = type
                args["VERSION"] = version
            }
            push("registry.jetbrains.team/p/paddle/docker/${type}") {
                tags(version)
            }
        }
    }
}
