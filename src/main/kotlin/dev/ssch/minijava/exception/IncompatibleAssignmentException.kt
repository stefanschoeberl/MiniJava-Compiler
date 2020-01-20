package dev.ssch.minijava.exception

import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.Token

class IncompatibleAssignmentException(expected: DataType, given: DataType?, token: Token) : SemanticException("Incompatible assignment, $expected was expected, but ${given ?: "void"} was given", token)