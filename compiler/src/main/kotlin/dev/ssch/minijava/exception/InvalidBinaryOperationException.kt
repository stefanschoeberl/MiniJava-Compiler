package dev.ssch.minijava.exception

import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.Token

class InvalidBinaryOperationException(a: DataType?, b: DataType?, operation: Token) : SemanticException("Cannot apply ${operation.text} on ${a ?: "void"} and ${b ?: "void"}", operation)