package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class IncompatibleTypeException(expected: DataType, given: DataType?, token: Token) : SemanticException("Incompatible type, $expected was expected, but ${given ?: "void"} was given", token)