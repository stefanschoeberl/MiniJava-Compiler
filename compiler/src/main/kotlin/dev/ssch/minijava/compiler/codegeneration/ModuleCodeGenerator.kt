package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.BuiltinFunctions
import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.ExternalFunctionNameProvider
import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.compiler.symboltable.StringLiteralSymbolTable
import dev.ssch.minijava.compiler.toWebAssemblyType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.FuncType
import dev.ssch.minijava.wasm.ast.Module
import dev.ssch.minijava.wasm.ast.ValueType

class ModuleCodeGenerator (
    private val classCodeGenerator: ClassCodeGenerator,
    private val codeEmitter: CodeEmitter,
    private val builtinFunctions: BuiltinFunctions,
    private val externalFunctionNameProvider: ExternalFunctionNameProvider
) {

    fun generateModule(classSymbolTable: ClassSymbolTable, trees: List<MiniJavaParser.MinijavaContext>): Pair<Module, StringLiteralSymbolTable> {
        codeEmitter.initModule(classSymbolTable)
        buildModuleBaseStructure()
        trees.flatMap {it.javaclass()}.forEach(classCodeGenerator::generate)
        return codeEmitter.buildModule()
    }

    private fun buildModuleBaseStructure() {
        importArrayFunctions()
        importNativeMethods()
        importConstructors()
        importGetterSetters()
        defineNormalMethods()
        defineInitializers()
    }

    private fun importInternal(name: String, parameters: MutableList<ValueType>, result: MutableList<ValueType>): Int {
        return codeEmitter.importFunction("internal", name, FuncType(parameters, result))
    }

    private fun importArrayFunctions() {
        builtinFunctions.newArrayNumericAddress = importInternal("new_array_numeric", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.newArrayBooleanAddress = importInternal("new_array_boolean", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.newArrayCharAddress = importInternal("new_array_char", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.newArrayReferenceAddress = importInternal("new_array_reference", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))

        builtinFunctions.arrayLengthAddress = importInternal("array_length", mutableListOf(ValueType.I32), mutableListOf(ValueType.I32))

        builtinFunctions.getArrayPrimitiveIntAddress = importInternal("get_array_numeric", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.getArrayPrimitiveFloatAddress = importInternal("get_array_numeric", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.F32))
        builtinFunctions.getArrayPrimitiveBooleanAddress = importInternal("get_array_boolean", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.getArrayPrimitiveCharAddress = importInternal("get_array_char", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.getArrayReferenceAddress = importInternal("get_array_reference", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))

        builtinFunctions.setArrayPrimitiveIntAddress = importInternal("set_array_numeric", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        builtinFunctions.setArrayPrimitiveFloatAddress = importInternal("set_array_numeric", mutableListOf(ValueType.I32, ValueType.I32, ValueType.F32), mutableListOf())
        builtinFunctions.setArrayPrimitiveBooleanAddress = importInternal("set_array_boolean", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        builtinFunctions.setArrayPrimitiveCharAddress = importInternal("set_array_char", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())
        builtinFunctions.setArrayReferenceAddress = importInternal("set_array_reference", mutableListOf(ValueType.I32, ValueType.I32, ValueType.I32), mutableListOf())

        builtinFunctions.concatStringStringAddress = importInternal("+_String_String", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatStringIntAddress = importInternal("+_String_numeric", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatIntStringAddress = importInternal("+_numeric_String", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatStringFloatAddress = importInternal("+_String_numeric", mutableListOf(ValueType.I32, ValueType.F32), mutableListOf(ValueType.I32))
        builtinFunctions.concatFloatStringAddress = importInternal("+_numeric_String", mutableListOf(ValueType.F32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatStringBooleanAddress = importInternal("+_String_boolean", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatBooleanStringAddress = importInternal("+_boolean_String", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatStringCharAddress = importInternal("+_String_char", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatCharStringAddress = importInternal("+_char_String", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatStringReferenceAddress = importInternal("+_String_reference", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
        builtinFunctions.concatReferenceStringAddress = importInternal("+_reference_String", mutableListOf(ValueType.I32, ValueType.I32), mutableListOf(ValueType.I32))
    }

    private fun importNativeMethods() {
        codeEmitter.classSymbolTable.classes.flatMap { classEntry ->
            classEntry.value.methodSymbolTable.nativeMethods.map {
                Pair(classEntry.key, it)
            }
        }.sortedBy { it.second.value.address }.forEach {
            val className = it.first
            val methodSignature = it.second.key
            val methodInfo = it.second.value

            codeEmitter.importFunction(
                "native",
                "$className.${externalFunctionNameProvider.externalNameForMethod(methodSignature)}",
                createFunctionType(methodSignature, methodInfo)
            )
        }
    }

    private fun importConstructors() {
        codeEmitter.classSymbolTable.classes.entries
            .sortedBy { it.value.constructorAddress }
            .forEach {
                val className = it.key
                codeEmitter.importFunction(
                    "native",
                    externalFunctionNameProvider.externalNameForConstructor(className),
                    FuncType(mutableListOf(), mutableListOf(ValueType.I32))
                )
            }
    }

    private fun importGetterSetters() {
        codeEmitter.classSymbolTable.classes.entries.flatMap { classEntry ->
            classEntry.value.fieldSymbolTable.fields.entries.map { fieldEntry ->
                Pair(classEntry.key, fieldEntry)
            }
        }.sortedBy { it.second.value.getterAddress }.forEach {
            val className = it.first
            val fieldName = it.second.key
            val fieldType = it.second.value.type.toWebAssemblyType()

            codeEmitter.importFunction(
                "native",
                externalFunctionNameProvider.externalNameForGetter(className, fieldName),
                FuncType(mutableListOf(ValueType.I32), mutableListOf(fieldType))
            )

            codeEmitter.importFunction(
                "native",
                externalFunctionNameProvider.externalNameForSetter(className, fieldName),
                FuncType(mutableListOf(ValueType.I32, fieldType), mutableListOf())
            )
        }
    }

    private fun defineNormalMethods() {
        codeEmitter.classSymbolTable.classes
            .flatMap { classEntry ->
                classEntry.value.methodSymbolTable.methods.entries.map { Pair(classEntry.key, it) }
            }
            .sortedBy { it.second.value.address }
            .forEach {
                val className = it.first
                val methodSignature = it.second.key
                val methodInfo = it.second.value

                val functionType = createFunctionType(methodSignature, methodInfo)
                val function = codeEmitter.declareFunction(functionType)

                codeEmitter.mapMethodToFunction(className, methodSignature, function)

                if (methodInfo.isPublic) {
                    val externalName = externalFunctionNameProvider.externalNameForMethod(methodSignature)
                    codeEmitter.exportFunction("$className.$externalName", methodInfo.address)
                }
            }
    }

    private fun defineInitializers() {
        codeEmitter.classSymbolTable.classes
            .flatMap { classEntry ->
                classEntry.value.initializerSymbolTable.initializers.entries.map { Pair(classEntry.key, it) }
            }
            .sortedBy { it.second.value.address }
            .forEach {
                val className = it.first
                val initializerSignature = it.second.key
                val functionType = createFunctionType(initializerSignature)
                val function = codeEmitter.declareFunction(functionType)
                codeEmitter.mapInitializerToFunction(className, initializerSignature, function)
            }
    }

    private fun createFunctionType(signature: MethodSymbolTable.MethodSignature, information: MethodSymbolTable.MethodInformation): FuncType {
        val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
        if (!information.isStatic) {
            parameters.add(0, ValueType.I32)
        }
        val returnType = information.returnType
            ?.let { type -> mutableListOf(type.toWebAssemblyType()) }
            ?: mutableListOf()
        return FuncType(parameters, returnType)
    }

    private fun createFunctionType(
        signature: InitializerSymbolTable.InitializerSignature
    ): FuncType {
        val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
        parameters.add(0, ValueType.I32)
        val returnType = ValueType.I32
        return FuncType(parameters, mutableListOf(returnType))
    }
}