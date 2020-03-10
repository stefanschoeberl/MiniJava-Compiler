package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaParser

class ClassCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generate(ctx: MiniJavaParser.JavaclassContext) {
        codeGenerationPhase.currentClass = ctx.name.text
        codeGenerationPhase.methodSymbolTable = codeGenerationPhase.classSymbolTable.getMethodSymbolTable(codeGenerationPhase.currentClass)
        codeGenerationPhase.visitChildren(ctx)
    }
}