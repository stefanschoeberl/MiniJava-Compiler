package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class RedefinedMethodException(name: String, token: Token) : SemanticException("$name has already been defined", token)