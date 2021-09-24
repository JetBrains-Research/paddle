package io.paddle.plugin.python.dependencies.parser

enum class PyRequirementMarkerRelation(val value: String) {
    IN("in"),
    NOT_IN("not in"),
    EQ("=="),
    NOT_EQ("!=")
}

data class PyRequirementMarker(
    val property: String,
    val relation: PyRequirementMarkerRelation,
    val value: String
)
