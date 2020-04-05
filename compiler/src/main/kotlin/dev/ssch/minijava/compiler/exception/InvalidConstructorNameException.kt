package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class InvalidConstructorNameException(name: String, expected: String, token: Token)
    : SemanticException("$name not valid as constructor name, $expected expected", token)