package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class ClassCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    private val visitor = Visitor()

    private inner class Visitor : MiniJavaBaseVisitor<Unit>() {
        override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
            codeGenerationPhase.methodCodeGenerator.generate(ctx)
        }

        override fun visitConstructor(ctx: MiniJavaParser.ConstructorContext) {
            codeGenerationPhase.methodCodeGenerator.generate(ctx)
        }
    }

    fun generate(ctx: MiniJavaParser.JavaclassContext) {
        codeGenerationPhase.currentClass = ctx.name.text
        codeGenerationPhase.methodSymbolTable = codeGenerationPhase.classSymbolTable.getMethodSymbolTable(codeGenerationPhase.currentClass)
        visitor.visitChildren(ctx)
    }
}