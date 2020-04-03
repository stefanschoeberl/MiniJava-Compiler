package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.LocalVariableSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class MethodCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val statementCodeGenerator: StatementCodeGenerator
) {
    fun generate(ctx: MiniJavaParser.MethodContext) {
        val methodName = ctx.name.text

        val parameters = beginMethod(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)

        if (codeEmitter.methodSymbolTable.isNative(methodName, parameterTypes)) {
            return
        }

        declareParameters(!codeEmitter.methodSymbolTable.isStatic(methodName, parameterTypes), parameters)

        codeEmitter.beginMethodGeneration(MethodSymbolTable.MethodSignature(methodName, parameterTypes))

        generateStatementExecution(ctx.statements)

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (codeEmitter.methodSymbolTable.returnTypeOf(methodName, parameterTypes) != null) {
            codeEmitter.emitInstruction(Instruction.unreachable)
        }

        codeEmitter.generateLocalVariables()
    }

    fun generate(ctx: MiniJavaParser.ConstructorContext) {
        val parameters = beginMethod(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)
        declareParameters(true, parameters)

        codeEmitter.beginInitializerGeneration(InitializerSymbolTable.InitializerSignature(parameterTypes))

        generateStatementExecution(ctx.statements)
        codeEmitter.emitInstruction(Instruction.local_get(codeEmitter.localsVariableSymbolTable.addressOfThis()))
        codeEmitter.generateLocalVariables()
    }

    private fun beginMethod(parameters: List<MiniJavaParser.FormalParameterContext>): List<Pair<String, DataType>> {
        // reset scope
        codeEmitter.localsVariableSymbolTable = LocalVariableSymbolTable()
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