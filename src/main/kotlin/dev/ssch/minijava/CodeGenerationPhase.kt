package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.codegeneration.*
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.InitializerSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable
import dev.ssch.minijava.symboltable.LocalVariableSymbolTable

class CodeGenerationPhase(val classSymbolTable: ClassSymbolTable) {

    private lateinit var module: Module
    lateinit var currentClass: String
    lateinit var currentFunction: Function

    lateinit var localsVariableSymbolTable: LocalVariableSymbolTable
    lateinit var methodSymbolTable: MethodSymbolTable

    lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>
    lateinit var initializers: MutableMap<Pair<String, InitializerSymbolTable.InitializerSignature>, Function>

    val operatorTable = OperatorTable()

    var mallocAddress: Int = -1

    val classCodeGenerator = ClassCodeGenerator(this)
    val methodCodeGenerator = MethodCodeGenerator(this)

    val arrayAccessExpressionCodeGeneration = ArrayAccessExpressionCodeGenerator(this)
    val memberAccessExpressionCodeGenerator = MemberExpressionCodeGenerator(this)

    val expressionCodeGenerator = ExpressionCodeGenerator(this)
    val statementCodeGenerator = StatementCodeGenerator(this)

    fun generateModule(trees: List<MiniJavaParser.MinijavaContext>): Module {
        beginModule()
        trees.flatMap {it.javaclass()}.forEach(classCodeGenerator::generate)
        endModule()
        return module
    }

    private fun beginModule() {
        module = Module()
        functions = mutableMapOf()
        initializers = mutableMapOf()

        importMalloc()
        importNativeMethods()
        importConstructors()
        importGetterSetters()
        defineNormalMethods()
        defineInitializers()
    }

    private fun importMalloc() {
        mallocAddress = module.importFunction(
            Import(
                "internal", "malloc",
                ImportDesc.Func(
                    declareFunctionType(
                        MethodSymbolTable.MethodSignature("malloc", listOf(DataType.PrimitiveType.Integer)),
                        MethodSymbolTable.MethodInformation(
                            -1, DataType.PrimitiveType.Integer,
                            isPublic = false,
                            isStatic = true
                        )
                    )
                )
            )
        )
    }

    private fun importNativeMethods() {
        classSymbolTable.classes.flatMap { classEntry ->
            classEntry.value.methodSymbolTable.nativeMethods.map {
                Pair(classEntry.key, it)
            }
        }.sortedBy { it.second.value.address }.forEach {
            val className = it.first
            val methodSignature = it.second.key
            val methodInfo = it.second.value

            val functionType = declareFunctionType(methodSignature, methodInfo)
            module.importFunction(
                Import(
                    "imports",
                    "$className.${methodSignature.externalName()}",
                    ImportDesc.Func(functionType)
                )
            )
        }
    }

    private fun importConstructors() {
        classSymbolTable.classes.entries
            .sortedBy { it.value.constructorAddress }
            .forEach {
                val className = it.key
                val functionType = module.declareType(FuncType(mutableListOf(), mutableListOf(ValueType.I32)))
                module.importFunction(
                    Import(
                        "imports",
                        externalConstructorName(className),
                        ImportDesc.Func(functionType)
                    )
                )
            }
    }

    private fun importGetterSetters() {
        classSymbolTable.classes.entries.flatMap { classEntry ->
            classEntry.value.fieldSymbolTable.fields.entries.map { fieldEntry ->
                Pair(classEntry.key, fieldEntry)
            }
        }.sortedBy { it.second.value.getterAddress }.forEach {
            val className = it.first
            val fieldName = it.second.key
            val fieldType = it.second.value.type.toWebAssemblyType()

            val getterType = module.declareType(FuncType(mutableListOf(ValueType.I32), mutableListOf(fieldType)))
            val setterType = module.declareType(FuncType(mutableListOf(ValueType.I32, fieldType), mutableListOf()))

            module.importFunction(
                Import(
                    "imports",
                    externalGetterName(className, fieldName),
                    ImportDesc.Func(getterType)
                )
            )

            module.importFunction(
                Import(
                    "imports",
                    externalSetterName(className, fieldName),
                    ImportDesc.Func(setterType)
                )
            )
        }
    }

    private fun defineNormalMethods() {
        classSymbolTable.classes
            .flatMap { classEntry ->
                classEntry.value.methodSymbolTable.methods.entries.map { Pair(classEntry.key, it) }
            }
            .sortedBy { it.second.value.address }
            .forEach {
                val className = it.first
                val methodSignature = it.second.key
                val methodInfo = it.second.value

                val functionType = declareFunctionType(methodSignature, methodInfo)
                val function = Function(functionType, mutableListOf(), Expr(mutableListOf()))
                module.declareFunction(function)
                functions[Pair(className, methodSignature)] = function
                if (methodInfo.isPublic) {
                    module.exports.add(
                        Export(
                            "$className.${methodSignature.externalName()}",
                            ExportDesc.Func(methodInfo.address)
                        )
                    )
                }
            }
    }

    private fun defineInitializers() {
        classSymbolTable.classes
            .flatMap { classEntry ->
                classEntry.value.initializerSymbolTable.initializers.entries.map { Pair(classEntry.key, it) }
            }
            .sortedBy { it.second.value.address }
            .forEach {
                val className = it.first
                val initializerSignature = it.second.key
                val functionType = declareFunctionType(initializerSignature)
                val function = Function(functionType, mutableListOf(), Expr(mutableListOf()))
                module.declareFunction(function)
                initializers[Pair(className, initializerSignature)] = function
            }
    }

    private fun declareFunctionType(
        signature: MethodSymbolTable.MethodSignature,
        information: MethodSymbolTable.MethodInformation
    ): Int {
        if (information.isStatic) {
            val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
            val returnType = information.returnType
                ?.let { type -> mutableListOf(type.toWebAssemblyType()) }
                ?: mutableListOf()
            return module.declareType(FuncType(parameters, returnType))
        } else {
            TODO()
        }
    }

    private fun declareFunctionType(
        signature: InitializerSymbolTable.InitializerSignature
    ): Int {
        val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
        parameters.add(0, ValueType.I32)
        val returnType = ValueType.I32
        return module.declareType(FuncType(parameters, mutableListOf(returnType)))
    }

    private fun endModule() {
        module.imports.add(Import("internal", "memory", ImportDesc.Memory(MemType(1))))
    }
}