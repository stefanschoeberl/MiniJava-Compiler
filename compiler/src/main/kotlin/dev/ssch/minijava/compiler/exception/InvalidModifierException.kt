package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class InvalidModifierException(name: String, token: Token) : SemanticException("Modifier $name is not allowed here", token)