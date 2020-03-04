package dev.ssch.minijava

import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class DeclarationPhase: MiniJavaBaseVisitor<Unit>() {

    val classSymbolTable = ClassSymbolTable()
    private lateinit var methodSymbolTable: MethodSymbolTable

    private var currentNativeMethodAddress = 1 // malloc
    private var currentMethodAddress = 0

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext) {
        visitChildren(ctx)
        recalculateMethodAddresses()
    }

    private fun recalculateMethodAddresses() {
        classSymbolTable.classes
            .flatMap { it.value.methodSymbolTable.methods.values }
            .forEach { it.address += currentNativeMethodAddress }
    }

    override fun visitJavaclass(ctx: MiniJavaParser.JavaclassContext) {
        val className = ctx.name.text
        // TODO check if class is not declared yet
        val classInformation = classSymbolTable.declareClass(className)

        methodSymbolTable = classInformation.methodSymbolTable
        visitChildren(ctx)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        val returnType = when (ctx.returntype.text) {
            "void" -> null
            else -> ctx.returntype.getDataType() ?: throw UnknownTypeException(ctx.returntype.text, ctx.returntype.start)
        }
        val name = ctx.name.text
        val parameters = ctx.parameters.map {
            it.type.getDataType() ?: throw UnknownTypeException(it.type.text, it.type.start)
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

        if (ctx.staticmodifier.size > 1) {
            throw InvalidModifierException(ctx.staticmodifier.last().text, ctx.staticmodifier.last())
        }

        if (ctx.nativemodifier.isEmpty()) {
            if (ctx.block == null) {
                throw MissingMethodBodyException(name, ctx.name)
            }
            methodSymbolTable.declareMethod(currentMethodAddress++, returnType, name, parameters,
                isPublic = ctx.publicmodifier.size == 1,
                isStatic = ctx.staticmodifier.size == 1
            )
        } else {
            if (ctx.semicolon == null) {
                throw InvalidMethodBodyException(name, ctx.name)
            }
            methodSymbolTable.declareNativeMethod(currentNativeMethodAddress++, returnType, name, parameters,
                isPublic = ctx.publicmodifier.size == 1,
                isStatic = ctx.staticmodifier.size == 1
            )
        }
    }
}