package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class UndefinedFieldException(name: String, token: Token) : SemanticException("$name is not defined", token)