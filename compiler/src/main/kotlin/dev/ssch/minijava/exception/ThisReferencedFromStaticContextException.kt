package dev.ssch.minijava.exception

import org.antlr.v4.runtime.Token

class ThisReferencedFromStaticContextException(token: Token) : SemanticException("this cannot be referenced from static context", token)