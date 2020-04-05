package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class RedefinedConstructorException(parameters: List<DataType>, token: Token)
    : SemanticException("Constructor with parameters (${parameters.joinToString()}) has already been defined", token)