package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Module
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class Compiler {

    fun compile(src: List<File>): Pair<Module, ClassSymbolTable> {

        val trees = src.map { file ->
            val input = CharStreams.fromStream(file.inputStream())
            val lexer = MiniJavaLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = MiniJavaParser(tokens)
            parser.errorHandler = BailErrorStrategy()
            parser.minijava()
        }

        val declarationPhase = DeclarationPhase()
        val classSymbolTable = declarationPhase.generateClassSymbolTable(trees)
        val codeGenerationPhase = CodeGenerationPhase(classSymbolTable)
        return Pair(codeGenerationPhase.generateModule(trees), classSymbolTable)
    }

}