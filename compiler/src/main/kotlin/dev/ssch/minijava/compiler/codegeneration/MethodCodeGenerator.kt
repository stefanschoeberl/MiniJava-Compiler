package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.compiler.symboltable.InitializerSymbolTable
import dev.ssch.minijava.compiler.symboltable.LocalVariableSymbolTable
import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable
import dev.ssch.minijava.compiler.toWebAssemblyType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class MethodCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {
    fun generate(ctx: MiniJavaParser.MethodContext) {
        val methodName = ctx.name.text

        val parameters = beginMethod(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)

        if (codeGenerationPhase.methodSymbolTable.isNative(methodName, parameterTypes)) {
            return
        }

        declareParameters(!codeGenerationPhase.methodSymbolTable.isStatic(methodName, parameterTypes), parameters)

        codeGenerationPhase.currentFunction = codeGenerationPhase.functions[Pair(codeGenerationPhase.currentClass, MethodSymbolTable.MethodSignature(methodName, parameterTypes))]!!

        generateStatementExecution(ctx.statements)

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (codeGenerationPhase.methodSymbolTable.returnTypeOf(methodName, parameterTypes) != null) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.unreachable)
        }

        generateLocalVariables()
    }

    fun generate(ctx: MiniJavaParser.ConstructorContext) {
        val parameters = beginMethod(ctx.parameters)
        val parameterTypes = extractParameterTypes(parameters)
        declareParameters(true, parameters)

        codeGenerationPhase.currentFunction = codeGenerationPhase.initializers[Pair(codeGenerationPhase.currentClass, InitializerSymbolTable.InitializerSignature(parameterTypes))]!!

        generateStatementExecution(ctx.statements)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_get(codeGenerationPhase.localsVariableSymbolTable.addressOfThis()))
        generateLocalVariables()
    }

    private fun beginMethod(parameters: List<MiniJavaParser.FormalParameterContext>): List<Pair<String, DataType>> {
        // reset scope
        codeGenerationPhase.localsVariableSymbolTable = LocalVariableSymbolTable()
        return parameters.map { Pair(it.name.text, it.type.getDataType(codeGenerationPhase.classSymbolTable)!!) }
    }

    private fun extractParameterTypes(parameters: List<Pair<String, DataType>>): List<DataType> {
        return parameters.map { it.second }
    }

    private fun declareParameters(withThisParameter: Boolean, parameters: List<Pair<String, DataType>>) {
        if (withThisParameter) {
            codeGenerationPhase.localsVariableSymbolTable.declareThisParameter()
        }
        parameters.forEach {
            codeGenerationPhase.localsVariableSymbolTable.declareParameter(it.first, it.second)
        }
    }

    private fun generateStatementExecution(statements: List<MiniJavaParser.StatementContext>) {
        statements.forEach(codeGenerationPhase.statementCodeGenerator::generateExecution)
    }

    private fun generateLocalVariables() {
        codeGenerationPhase.localsVariableSymbolTable.allLocalVariables.forEach {
            codeGenerationPhase.currentFunction.locals.add(it.toWebAssemblyType())
        }
    }
}