package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class MethodCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val statementCodeGenerator: StatementCodeGenerator
) {
    fun generate(ctx: MiniJavaParser.MethodContext) {
        val methodName = ctx.name.text

        val parameters = processFormalParameters(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)

        if (codeEmitter.methodSymbolTable.isNative(methodName, parameterTypes)) {
            return
        }

        codeEmitter.switchToMethod(MethodSymbolTable.MethodSignature(methodName, parameterTypes))
        declareParameters(!codeEmitter.methodSymbolTable.isStatic(methodName, parameterTypes), parameters)

        generateStatementExecution(ctx.statements)

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (codeEmitter.methodSymbolTable.returnTypeOf(methodName, parameterTypes) != null) {
            codeEmitter.emitInstruction(Instruction.unreachable)
        }

        codeEmitter.generateLocalVariables()
    }

    fun generate(ctx: MiniJavaParser.ConstructorContext) {
        val parameters = processFormalParameters(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)

        codeEmitter.switchToInitializer(InitializerSymbolTable.InitializerSignature(parameterTypes))
        declareParameters(true, parameters)

        generateStatementExecution(ctx.statements)
        codeEmitter.emitInstruction(Instruction.local_get(codeEmitter.localsVariableSymbolTable.addressOfThis()))
        codeEmitter.generateLocalVariables()
    }

    private fun processFormalParameters(parameters: List<MiniJavaParser.FormalParameterContext>): List<Pair<String, DataType>> {
        return parameters.map { Pair(it.name.text, it.type.getDataType(codeEmitter.classSymbolTable)!!) }
    }

    private fun extractParameterTypes(parameters: List<Pair<String, DataType>>): List<DataType> {
        return parameters.map { it.second }
    }

    private fun declareParameters(withThisParameter: Boolean, parameters: List<Pair<String, DataType>>) {
        if (withThisParameter) {
            codeEmitter.localsVariableSymbolTable.declareThisParameter()
        }
        parameters.forEach {
            codeEmitter.localsVariableSymbolTable.declareParameter(it.first, it.second)
        }
    }

    private fun generateStatementExecution(statements: List<MiniJavaParser.StatementContext>) {
        statements.forEach(statementCodeGenerator::generateExecution)
    }
}