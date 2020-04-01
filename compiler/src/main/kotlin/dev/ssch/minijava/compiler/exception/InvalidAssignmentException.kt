package dev.ssch.minijava.compiler.exception

import org.antlr.v4.runtime.Token

class InvalidAssignmentException(token: Token) : SemanticException("Invalid assignment, variable expected", token)