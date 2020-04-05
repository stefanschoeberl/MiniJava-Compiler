package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.*
import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.compiler.symboltable.FieldSymbolTable
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class DeclarationPhase {

    private lateinit var classSymbolTable: ClassSymbolTable
    private lateinit var methodSymbolTable: MethodSymbolTable
    private lateinit var fieldSymbolTable: FieldSymbolTable
    private lateinit var initializerSymbolTable: InitializerSymbolTable

    private var currentNativeMethodAddress = 25 // builtin functions
    private var currentMethodAddress = 0
    private var currentInitializerAddress = 0
    private var currentConstructorAddress = 0
    private var currentGetterSetterAddress = 0

    private var declareOnly = true

    private val visitor = Visitor()

    fun generateClassSymbolTable(trees: List<MiniJavaParser.MinijavaContext>): ClassSymbolTable {
        classSymbolTable = ClassSymbolTable()

        declareOnly = true
        trees.forEach(visitor::visitChildren)
        declareOnly = false
        trees.forEach(visitor::visitChildren)

        recalculateMethodAddresses()

        return classSymbolTable
    }

    private fun recalculateMethodAddresses() {
        // Address Layout:
        // native methods
        // constructors
        // getter/setter
        // normal methods
        // initializer

        var currentOffset = currentNativeMethodAddress

        classSymbolTable.classes.forEach { it.value.constructorAddress += currentOffset }

        currentOffset += currentConstructorAddress

        classSymbolTable.classes
            .flatMap { it.value.fieldSymbolTable.fields.values }
            .forEach {
                it.getterAddress += currentOffset
                it.setterAddress += currentOffset
            }

        currentOffset += currentGetterSetterAddress

        classSymbolTable.classes
            .flatMap { it.value.methodSymbolTable.methods.values }
            .forEach { it.address += currentOffset }

        currentOffset += currentMethodAddress

        classSymbolTable.classes
            .flatMap { it.value.initializerSymbolTable.initializers.values }
            .forEach { it.address += currentOffset }
    }

    private inner class Visitor : MiniJavaBaseVisitor<Unit>() {
        private lateinit var currentClassName: String

        override fun visitJavaclass(ctx: MiniJavaParser.JavaclassContext) {
            val className = ctx.name.text
            if (declareOnly) {
                if (classSymbolTable.isDeclared(className)) {
                    throw RedefinedClassException(className, ctx.name)
                }

                classSymbolTable.declareClass(currentConstructorAddress++, className)
            } else {
                methodSymbolTable = classSymbolTable.getMethodSymbolTable(className)
                fieldSymbolTable = classSymbolTable.getFieldSymbolTable(className)
                initializerSymbolTable = classSymbolTable.getInitializerSymbolTable(className)
                currentClassName = className
                visitChildren(ctx)
            }
        }

        override fun visitField(ctx: MiniJavaParser.FieldContext) {
            val type = ctx.type.getDataType(classSymbolTable) ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)
            val name = ctx.name.text
            if (fieldSymbolTable.isDeclared(name)) {
                throw RedefinedFieldException(name, ctx.name)
            }
            val getterAddress = currentGetterSetterAddress++
            val setterAddress = currentGetterSetterAddress++
            fieldSymbolTable.declareField(getterAddress, setterAddress, name, type)
        }

        override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
            val returnType = when (ctx.returntype.text) {
                "void" -> null
                else -> ctx.returntype.getDataType(classSymbolTable) ?: throw UnknownTypeException(
                    ctx.returntype.text,
                    ctx.returntype.start
                )
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
                methodSymbolTable.declareMethod(
                    currentMethodAddress++, returnType, name, parameters,
                    isPublic = ctx.publicmodifier.size == 1,
                    isStatic = ctx.staticmodifier.size == 1
                )
            } else {
                if (ctx.semicolon == null) {
                    throw InvalidMethodBodyException(name, ctx.name)
                }
                methodSymbolTable.declareNativeMethod(
                    currentNativeMethodAddress++, returnType, name, parameters,
                    isPublic = ctx.publicmodifier.size == 1,
                    isStatic = ctx.staticmodifier.size == 1
                )
            }
        }

        override fun visitConstructor(ctx: MiniJavaParser.ConstructorContext) {
            if (ctx.name.text != currentClassName) {
                throw InvalidConstructorNameException(ctx.name.text, currentClassName, ctx.name)
            }
            val parameters = ctx.parameters.map {
                it.type.getDataType(classSymbolTable) ?: throw UnknownTypeException(it.type.text, it.type.start)
            }

            if (initializerSymbolTable.isDeclared(parameters)) {
                throw RedefinedConstructorException(parameters, ctx.name)
            }

            initializerSymbolTable.declareInitializer(currentInitializerAddress++, parameters)
        }
    }
}