package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class VoidParameterException(token: Token) : SemanticException("Cannot pass void to method", token)