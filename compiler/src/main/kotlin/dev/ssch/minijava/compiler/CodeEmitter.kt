package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.LocalVariableSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.wasm.ast.*
import dev.ssch.minijava.wasm.ast.Function

class CodeEmitter {

    lateinit var classSymbolTable: ClassSymbolTable
        private set
    lateinit var methodSymbolTable: MethodSymbolTable
        private set
    lateinit var localsVariableSymbolTable: LocalVariableSymbolTable
        private set

    private lateinit var module: Module
    lateinit var currentClass: String
        private set
    private lateinit var currentFunction: Function

    private lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>
    private lateinit var initializers: MutableMap<Pair<String, InitializerSymbolTable.InitializerSignature>, Function>

    fun emitInstruction(instruction: Instruction) {
        currentFunction.body.instructions.add(instruction)
    }

    fun emitInstruction(index: Int, instruction: Instruction) {
        currentFunction.body.instructions.add(index, instruction)
    }

    fun emitInstructions(instruction: Collection<Instruction>) {
        currentFunction.body.instructions.addAll(instruction)
    }

    fun deleteInstruction(index: Int) {
        currentFunction.body.instructions.removeAt(index)
    }

    fun deleteInstructionsBeginningAt(n: Int) {
        while (currentFunction.body.instructions.size > n) {
            deleteInstruction(currentFunction.body.instructions.size - 1)
        }
    }

    val nextInstructionAddress: Int
        get() = currentFunction.body.instructions.size

    fun generateLocalVariables() {
        currentFunction.locals.addAll(localsVariableSymbolTable.allLocalVariables.map(DataType::toWebAssemblyType))
    }

    fun importFunction(moduleName: String, functionName: String, type: FuncType): Int {
        return module.importFunction(
            Import(
                moduleName, functionName,
                ImportDesc.Func(
                    module.declareType(type)
                )
            )
        )
    }

    fun declareFunction(type: FuncType): Function {
        val function = Function(module.declareType(type), mutableListOf(), Expr(mutableListOf()))
        module.declareFunction(function)
        return function
    }

    fun mapMethodToFunction(className: String, methodSignature: MethodSymbolTable.MethodSignature, function: Function) {
        functions[Pair(className, methodSignature)] = function
    }

    fun mapInitializerToFunction(className: String, initializerSignature: InitializerSymbolTable.InitializerSignature, function: Function) {
        initializers[Pair(className, initializerSignature)] = function
    }

    fun exportFunction(functionName: String, address: Int) {
        module.exports.add(
            Export(functionName, ExportDesc.Func(address))
        )
    }

    fun initModule(classSymbolTable: ClassSymbolTable) {
        module = Module()
        functions = mutableMapOf()
        initializers = mutableMapOf()
        this.classSymbolTable = classSymbolTable
    }

    fun buildModule(): Module {
        return module
    }

    fun switchToClass(className: String) {
        currentClass = className
        methodSymbolTable = classSymbolTable.getMethodSymbolTable(currentClass)
    }

    fun switchToMethod(methodSignature: MethodSymbolTable.MethodSignature) {
        resetScope()
        currentFunction = functions[Pair(currentClass, methodSignature)]!!
    }

    fun switchToInitializer(initializerSignature: InitializerSymbolTable.InitializerSignature) {
        resetScope()
        currentFunction = initializers[Pair(currentClass, initializerSignature)]!!
    }

    private fun resetScope() {
        localsVariableSymbolTable = LocalVariableSymbolTable()
    }
}
