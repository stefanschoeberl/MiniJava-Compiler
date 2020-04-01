package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class ExpressionIsNotAnArrayException(token: Token) : SemanticException("Expression is not an array", token)