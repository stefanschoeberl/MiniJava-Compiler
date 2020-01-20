package dev.ssch.minijava

import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class DeclarationPhase: MiniJavaBaseVisitor<Unit>() {

    val methodSymbolTable = MethodSymbolTable()

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext?) {
//        methodSymbolTable.declareNativeMethod(null, "println", listOf(DataType.Integer), false)
//        methodSymbolTable.declareNativeMethod(null, "println", listOf(DataType.Boolean), false)
        visitChildren(ctx)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        val returnType = when (ctx.returntype.text) {
            "void" -> null
            else -> DataType.fromString(ctx.returntype.text) ?: throw UnknownTypeException(ctx.returntype.text, ctx.returntype)
        }
        val name = ctx.name.text
        val parameters = ctx.parameters.map {
            DataType.fromString(it.type.text) ?: throw UnknownTypeException(it.type.text, it.type)
        }
        if (methodSymbolTable.isDeclared(name, parameters)) {
            throw RedefinedMethodException(name, ctx.name)
        }

        if (ctx.publicmodifier.size > 1) {
            throw InvalidModifierException(ctx.publicmodifier.last().text, ctx.publicmodifier.last())
        }

        if (ctx.nativemodifier.size > 1) {
            throw InvalidModifierException(ctx.nativemodifier.last().text, ctx.nativemodifier.last())
        }

        if (ctx.nativemodifier.isEmpty()) {
            if (ctx.block == null) {
                throw MissingMethodBodyException(name, ctx.name)
            }
            methodSymbolTable.declareMethod(returnType, name, parameters, ctx.publicmodifier.size == 1)
        } else {
            if (ctx.semicolon == null) {
                throw InvalidMethodBodyException(name, ctx.name)
            }
            methodSymbolTable.declareNativeMethod(returnType, name, parameters, ctx.publicmodifier.size == 1)
        }
    }
}