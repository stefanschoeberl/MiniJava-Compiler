package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.codegeneration.ModuleCodeGenerator
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class Compiler (
    private val declarationPhase: DeclarationPhase,
    private val moduleCodeGenerator: ModuleCodeGenerator
) {

    fun expandSourceDefinitions(src: List<File>): List<File> {
        return src.flatMap { file ->
            if (file.isDirectory) {
                file.listFiles()!!.filter {
                    it.isFile && it.name.endsWith(".minijava")
                }
            } else {
                listOf(file)
            }
        }
    }

    fun compile(src: List<File>): Bundle {

        val trees = src.map { file ->
            val input = CharStreams.fromStream(file.inputStream())
            val lexer = MiniJavaLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = MiniJavaParser(tokens)
            parser.errorHandler = BailErrorStrategy()
            parser.minijava()
        }

        val classSymbolTable = declarationPhase.generateClassSymbolTable(trees)
        val (module, stringLiteralSymbolTable) = moduleCodeGenerator.generateModule(classSymbolTable, trees)
        return Bundle(module, classSymbolTable, stringLiteralSymbolTable)
    }

}