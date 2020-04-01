package dev.ssch.minijava.exception

import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.Token

class InconvertibleTypeException(from: DataType?, to: DataType, token: Token) : SemanticException("Cannot cast ${from ?: "void"} to $to", token)