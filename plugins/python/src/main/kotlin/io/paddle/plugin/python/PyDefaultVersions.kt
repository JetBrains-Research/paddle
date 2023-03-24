package io.paddle.plugin.python

import io.paddle.plugin.python.dependencies.interpretator.InterpreterVersion
import io.paddle.plugin.python.dependencies.packages.PyPackageVersionSpecifier

/**
 * Versions of the dev packages installed by Paddle automatically (the latest stable versions at the moment of writing).
 */
object PyDefaultVersions {
    val PYTEST = PyPackageVersionSpecifier.fromString("7.1.2")
    val TWINE = PyPackageVersionSpecifier.fromString("4.0.1")
    val PYLINT = PyPackageVersionSpecifier.fromString("2.14.4")
    val MYPY = PyPackageVersionSpecifier.fromString("0.961")
    val WHEEL = PyPackageVersionSpecifier.fromString("0.37.1")
    val SETUPTOOLS = PyPackageVersionSpecifier.fromString("60.2.0")
    val PYTHON = InterpreterVersion("3.8")
}
