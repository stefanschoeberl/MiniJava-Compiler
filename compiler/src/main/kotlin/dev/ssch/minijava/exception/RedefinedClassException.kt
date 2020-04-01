package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class RedefinedClassException(name: String, token: Token) : SemanticException("$name has already been defined", token)