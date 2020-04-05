package dev.ssch.minijava.compiler.exception

import dev.ssch.minijava.compiler.DataType
import org.antlr.v4.runtime.Token

class UndefinedConstructorException(parameters: List<DataType>, token: Token)
    : SemanticException("Constructor with parameters (${parameters.joinToString()}) is not defined", token)