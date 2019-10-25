package dev.ssch.minijava.exception

import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.Token

class InvalidUnaryOperationException(operand: DataType, operation: Token) : SemanticException("Cannot apply ${operation.text} on $operand", operation)