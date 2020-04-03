package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class ClassCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val methodCodeGenerator: MethodCodeGenerator
) {

    private val visitor = Visitor()

    private inner class Visitor : MiniJavaBaseVisitor<Unit>() {
        override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
            methodCodeGenerator.generate(ctx)
        }

        override fun visitConstructor(ctx: MiniJavaParser.ConstructorContext) {
            methodCodeGenerator.generate(ctx)
        }
    }

    fun generate(ctx: MiniJavaParser.JavaclassContext) {
        codeEmitter.switchToClass(ctx.name.text)
        visitor.visitChildren(ctx)
    }
}