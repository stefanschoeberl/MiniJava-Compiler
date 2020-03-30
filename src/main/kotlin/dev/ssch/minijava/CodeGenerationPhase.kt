package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.codegeneration.*
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.InitializerSymbolTable
import dev.ssch.minijava.symboltable.LocalVariableSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable

class CodeGenerationPhase(val classSymbolTable: ClassSymbolTable) {

    private lateinit var module: Module
    lateinit var currentClass: String
    lateinit var currentFunction: Function

    lateinit var localsVariableSymbolTable: LocalVariableSymbolTable
    lateinit var methodSymbolTable: MethodSymbolTable

    lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>
    lateinit var initializers: MutableMap<Pair<String, InitializerSymbolTable.InitializerSignature>, Function>

    val operatorTable = OperatorTable()

    var newArrayNumericAddress: Int = -1
    var newArrayBooleanAddress: Int = -1
    var newArrayCharAddress: Int = -1
    var newArrayReferenceAddress: Int = -1

    var getArrayPrimitiveIntAddress: Int = -1
    var getArrayPrimitiveFloatAddress: Int = -1
    var getArrayPrimitiveBooleanAddress: Int = -1
    var getArrayPrimitiveCharAddress: Int = -1
    var getArrayReferenceAddress: Int = -1

    var setArrayPrimitiveIntAddress: Int = -1
    var setArrayPrimitiveFloatAddress: Int = -1
    var setArrayPrimitiveBooleanAddress: Int = -1
    var setArrayPrimitiveCharAddress: Int = -1
    var setArrayReferenceAddress: Int = -1

    private val classCodeGenerator = ClassCodeGenerator(this)
    val methodCodeGenerator = MethodCodeGenerator(this)

    val arrayAccessExpressionCodeGeneration = ArrayAccessExpressionCodeGenerator(this)
    val memberAccessExpressionCodeGenerator = MemberExpressionCodeGenerator(this)
    val basicExpressionCodeGenerator = BasicExpressionCodeGenerator(this)

    val variableAssignmentStatementCodeGenerator = VariableAssignmentStatementCodeGenerator(this)
    val expressionCodeGenerator = ExpressionCodeGenerator(this)
    val statementCodeGenerator = StatementCodeGenerator(this)

    fun generateModule(trees: List<MiniJavaParser.MinijavaContext>): Module {
        initModule()
        trees.flatMap {it.javaclass()}.forEach(classCodeGenerator::generate)
        return module
    }

    private fun initModule() {
        module = Module()
        functions = mutableMapOf()
        initializers = mutableMapOf()

        importArrayFunctions()
        importNativeMethods()
        importConstructors()
        importGetterSetters()
        defineNormalMethods()
        defineInitializers()
    }

    private fun importInternal(name: String, parameters: MutableList<ValueType>, result: MutableList<ValueType>): Int {
        return module.importFunction(
            Import(
                "internal", name,
                ImportDesc.Func(
                    module.declareType(FuncType(parameters, result))
                )
            )
        )
    }

    private fun importArrayFunctions() {
        newArrayNumericAddress = importInternal("new_array_numeric", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        newArrayBooleanAddress = importInternal("new_array_boolean", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        newArrayCharAddress = importInternal("new_array_char", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        newArrayReferenceAddress = importInternal("new_array_reference", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))

        getArrayPrimitiveIntAddress = importInternal("get_array_numeric", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        getArrayPrimitiveFloatAddress = importInternal("get_array_numeric", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.F32))
        getArrayPrimitiveBooleanAddress = importInternal("get_array_boolean", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        getArrayPrimitiveCharAddress = importInternal("get_array_char", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        getArrayReferenceAddress = importInternal("get_array_reference", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))

        setArrayPrimitiveIntAddress = importInternal("set_array_numeric", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        setArrayPrimitiveFloatAddress = importInternal("set_array_numeric", mutableListOf(ValueType.I32, ValueType.I32, ValueType.F32), mutableListOf())
        setArrayPrimitiveBooleanAddress = importInternal("set_array_boolean", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        setArrayPrimitiveCharAddress = importInternal("set_array_char", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        setArrayReferenceAddress = importInternal("set_array_reference", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
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
        val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
        if (!information.isStatic) {
            parameters.add(0, ValueType.I32)
        }
        val returnType = information.returnType
            ?.let { type -> mutableListOf(type.toWebAssemblyType()) }
            ?: mutableListOf()
        return module.declareType(FuncType(parameters, returnType))
    }

    private fun declareFunctionType(
        signature: InitializerSymbolTable.InitializerSignature
    ): Int {
        val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
        parameters.add(0, ValueType.I32)
        val returnType = ValueType.I32
        return module.declareType(FuncType(parameters, mutableListOf(returnType)))
    }
}