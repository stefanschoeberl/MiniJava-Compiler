package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class InvalidUnaryOperationException(operand: DataType?, operation: Token) : SemanticException("Cannot apply ${operation.text} on ${operand ?: "void"}", operation)