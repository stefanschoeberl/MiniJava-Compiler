package dev.ssch.minijava

import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.ConstructorSymbolTable
import dev.ssch.minijava.symboltable.FieldSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable

class DeclarationPhase: MiniJavaBaseVisitor<Unit>() {

    val classSymbolTable = ClassSymbolTable()
    private lateinit var methodSymbolTable: MethodSymbolTable
    private lateinit var fieldSymbolTable: FieldSymbolTable
    private lateinit var constructorSymbolTable: ConstructorSymbolTable

    private var currentNativeMethodAddress = 1 // malloc
    private var currentMethodAddress = 0
    private var currentConstructorAddress = 0

    var declareOnly = true

    fun process(trees: List<MiniJavaParser.MinijavaContext>) {
        declareOnly = true
        trees.forEach(this::visitChildren)
        declareOnly = false
        trees.forEach(this::visitChildren)

        recalculateMethodAddresses()
    }

    private fun recalculateMethodAddresses() {
        classSymbolTable.classes
            .flatMap { it.value.methodSymbolTable.methods.values }
            .forEach { it.address += currentNativeMethodAddress }

        classSymbolTable.classes
            .flatMap { it.value.constructorSymbolTable.constructors.values }
            .forEach { it.address += currentMethodAddress + currentNativeMethodAddress }
    }

    override fun visitJavaclass(ctx: MiniJavaParser.JavaclassContext) {
        val className = ctx.name.text
        if (declareOnly) {
            if (classSymbolTable.isDeclared(className)) {
                throw RedefinedClassException(className, ctx.name)
            }

            classSymbolTable.declareClass(className)
        } else {
            methodSymbolTable = classSymbolTable.getMethodSymbolTable(className)
            fieldSymbolTable = classSymbolTable.getFieldSymbolTable(className)
            constructorSymbolTable = classSymbolTable.getConstructorSymbolTable(className)
            visitChildren(ctx)
        }
    }

    override fun visitField(ctx: MiniJavaParser.FieldContext) {
        val type = ctx.type.getDataType(classSymbolTable) ?: TODO()
        val name = ctx.name.text
        if (fieldSymbolTable.isDeclared(name)) {
            TODO()
        }

        fieldSymbolTable.declareField(name, type)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        val returnType = when (ctx.returntype.text) {
            "void" -> null
            else -> ctx.returntype.getDataType(classSymbolTable) ?: throw UnknownTypeException(ctx.returntype.text, ctx.returntype.start)
        }
        val name = ctx.name.text
        val parameters = ctx.parameters.map {
            it.type.getDataType(classSymbolTable) ?: throw UnknownTypeException(it.type.text, it.type.start)
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

    override fun visitConstructor(ctx: MiniJavaParser.ConstructorContext) {
        val parameters = ctx.parameters.map {
            it.type.getDataType(classSymbolTable) ?: throw UnknownTypeException(it.type.text, it.type.start)
        }

        if (constructorSymbolTable.isDeclared(parameters)) {
            TODO()
        }

        constructorSymbolTable.declareConstructor(currentConstructorAddress++, parameters)
    }
}