package io.paddle.plugin.python.dependencies.packages

enum class PyPackageVersionRelation(val operator: String) {
    LTE("<="),
    GTE(">="),
    STR_EQ("==="),
    LT("<"),
    GT(">"),
    EQ("=="),
    NE("!="),
    COMPATIBLE("~=")
}

typealias PyPackageVersion = String

class PyPackageVersionSpecifier private constructor(val clauses: List<PyPackageVersionClause>) {
    data class PyPackageVersionClause(val relation: PyPackageVersionRelation, val version: PyPackageVersion) {
        override fun toString(): String = "${relation.operator}${version}"
    }

    companion object {
        fun fromString(versionSpecifier: String): PyPackageVersionSpecifier {
            return versionSpecifier.split(",").map { it.trim() }.map { clause ->
                val relation = PyPackageVersionRelation.values().find { clause.startsWith(it.operator) }
                if (relation == null) {
                    // the relation is not specified, assuming it is meant to be == (EQ)
                    PyPackageVersionClause(PyPackageVersionRelation.EQ, clause.trim())
                } else {
                    val rawVersion = clause.substringAfter(relation.operator).trim()
                    PyPackageVersionClause(relation, rawVersion)
                }
            }.let { PyPackageVersionSpecifier(it) }
        }
    }

    override fun toString(): String {
        return clauses.joinToString(", ")
    }
}
