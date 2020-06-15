package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class InstanceMethodCallFromStaticMethodException(className: String, methodName: String, token: Token) : SemanticException("Cannot call instance method $className.$methodName in static method", token)