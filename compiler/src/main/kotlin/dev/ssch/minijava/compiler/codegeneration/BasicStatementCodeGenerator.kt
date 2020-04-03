package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class BasicStatementCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val statementCodeGenerator: StatementCodeGenerator
) {

    fun generateExecution(ctx: MiniJavaParser.ExpressionStmtContext) {
        if (ctx.expr() is MiniJavaParser.CallExprContext) {
            val exprType = expressionCodeGenerator.generateEvaluation(ctx.expr())

            if (exprType != null) {
                codeEmitter.emitInstruction(Instruction.drop)
            }
        } else {
            TODO()
        }
    }

    fun generateExecution(ctx: MiniJavaParser.ReturnStmtContext) {
        expressionCodeGenerator.generateEvaluation(ctx.value)
        codeEmitter.emitInstruction(Instruction._return)
    }

    fun generateExecution(ctx: MiniJavaParser.BlockStmtContext) {
        codeEmitter.localsVariableSymbolTable.pushScope()
        ctx.statements.forEach(statementCodeGenerator::generateExecution)
        codeEmitter.localsVariableSymbolTable.popScope()
    }
}