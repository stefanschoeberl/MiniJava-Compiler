package dev.ssch.minijava

import dev.ssch.minijava.ast.Module
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class Compiler {

    fun compile(src: String): Module {
        val input = CharStreams.fromString(src)
        val lexer = MiniJavaLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = MiniJavaParser(tokens)
        parser.errorHandler = BailErrorStrategy()
        val tree = parser.minijava()

        val declarationPhase = DeclarationPhase()
        declarationPhase.visit(tree)

        val codeGenerationPhase = CodeGenerationPhase(declarationPhase.classSymbolTable)
        return codeGenerationPhase.generateModule(tree)
    }

}