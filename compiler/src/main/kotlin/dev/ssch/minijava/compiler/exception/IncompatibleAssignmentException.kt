package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class IncompatibleAssignmentException(expected: DataType?, given: DataType?, token: Token) : SemanticException("Incompatible assignment, $expected was expected, but ${given ?: "void"} was given", token)