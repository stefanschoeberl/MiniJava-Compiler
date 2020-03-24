package dev.ssch.minijava

import dev.ssch.minijava.ast.Module
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class Compiler {

    fun compile(src: List<File>): Module {

        val trees = src.map { file ->
            val input = CharStreams.fromStream(file.inputStream())
            val lexer = MiniJavaLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = MiniJavaParser(tokens)
            parser.errorHandler = BailErrorStrategy()
            parser.minijava()
        }

        val declarationPhase = DeclarationPhase()
        declarationPhase.process(trees)
        val codeGenerationPhase = CodeGenerationPhase(declarationPhase.classSymbolTable)
        return codeGenerationPhase.generateModule(trees)
    }

}