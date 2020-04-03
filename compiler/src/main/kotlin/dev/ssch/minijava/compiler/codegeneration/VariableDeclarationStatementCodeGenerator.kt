package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.assignTypeTo
import dev.ssch.minijava.compiler.exception.IncompatibleAssignmentException
import dev.ssch.minijava.compiler.exception.RedefinedVariableException
import dev.ssch.minijava.compiler.exception.UnknownTypeException
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction
import org.antlr.v4.runtime.Token

class VariableDeclarationStatementCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator
) {

    fun generate(ctx: MiniJavaParser.VardeclStmtContext) {
        declareVariable(ctx.name, ctx.type)
    }

    fun generate(ctx: MiniJavaParser.VardeclassignStmtContext) {
        val exprType = expressionCodeGenerator.generateEvaluation(ctx.expr())

        val (variableName, variableType) = declareVariable(ctx.name, ctx.type)

        val conversionCode = exprType?.assignTypeTo(variableType)
            ?: throw IncompatibleAssignmentException(variableType, exprType, ctx.name)

        codeEmitter.emitInstructions(conversionCode)

        codeEmitter.emitInstruction(Instruction.local_set(codeEmitter.localsVariableSymbolTable.addressOf(variableName)))
    }

    private fun declareVariable(nameToken: Token, typeContext: MiniJavaParser.TypeDefinitionContext): Pair<String, DataType> {
        val name = nameToken.text
        val type = typeContext.getDataType(codeEmitter.classSymbolTable)
            ?: throw UnknownTypeException(typeContext.text, typeContext.start)
        if (codeEmitter.localsVariableSymbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, nameToken)
        }
        codeEmitter.localsVariableSymbolTable.declareVariable(name, type)
        return Pair(name, type)
    }
}