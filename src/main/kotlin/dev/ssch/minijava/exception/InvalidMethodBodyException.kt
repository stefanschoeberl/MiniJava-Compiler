package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class InvalidMethodBodyException(name: String, token: Token) : SemanticException("Method $name cannot have a method body", token)