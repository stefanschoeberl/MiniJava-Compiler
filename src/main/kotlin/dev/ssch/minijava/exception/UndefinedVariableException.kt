package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class UndefinedVariableException(name: String, token: Token) : SemanticException("$name is not defined", token)