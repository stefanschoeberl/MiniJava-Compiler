package dev.ssch.minijava.exception

import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.Token

class IncompatibleTypeException(expected: DataType, given: DataType?, token: Token) : SemanticException("Incompatible type, $expected was expected, but ${given ?: "void"} was given", token)