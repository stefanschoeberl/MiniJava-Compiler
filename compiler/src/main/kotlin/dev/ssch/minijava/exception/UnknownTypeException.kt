package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class UnknownTypeException(name: String, token: Token) : SemanticException("$name is an unknown type", token)