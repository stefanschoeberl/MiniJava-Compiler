package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class NotAReferenceTypeException(type: DataType?, token: Token)
    : SemanticException("${type?.toString() ?: "void"} is not a reference type", token)