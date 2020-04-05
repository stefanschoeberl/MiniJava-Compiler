package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class NotACallableExpressionException(code: String, token: Token) : SemanticException("$code is not a callable expression", token)