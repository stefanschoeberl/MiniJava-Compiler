package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class VoidParameterException(token: Token) : SemanticException("Cannot pass void to method", token)