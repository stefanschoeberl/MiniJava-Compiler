package dev.ssch.minijava

import dev.ssch.minijava.exception.RedefinedMethodException
import dev.ssch.minijava.exception.UnknownTypeException
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class DeclarationPhase: MiniJavaBaseVisitor<Unit>() {

    val methodSymbolTable = MethodSymbolTable()

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext?) {
        methodSymbolTable.declareNativeMethod(null, "println", listOf(DataType.Integer), false)
        methodSymbolTable.declareNativeMethod(null, "println", listOf(DataType.Boolean), false)
        visitChildren(ctx)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        val returnType = when (ctx.returntype.text) {
            "void" -> null
            else -> DataType.fromString(ctx.returntype.text) ?: throw UnknownTypeException(ctx.returntype.text, ctx.returntype)
        }
        val name = ctx.name.text
        val parameters = listOf<DataType>()
        if (methodSymbolTable.isDeclared(name, parameters)) {
            throw RedefinedMethodException(name, ctx.name)
        }

        methodSymbolTable.declareMethod(returnType, name, parameters, ctx.publicmodifier != null)
    }
}