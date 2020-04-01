package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class UnknownTypeException(name: String, token: Token) : SemanticException("$name is an unknown type", token)