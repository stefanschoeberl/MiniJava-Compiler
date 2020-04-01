package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class MissingMethodBodyException(name: String, token: Token) : SemanticException("Method $name is missing a method body", token)