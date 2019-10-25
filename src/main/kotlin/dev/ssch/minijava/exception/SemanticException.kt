package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

open class SemanticException(message: String, token: Token) :
    RuntimeException("Error at ${token.line}:${token.charPositionInLine + 1}: $message")