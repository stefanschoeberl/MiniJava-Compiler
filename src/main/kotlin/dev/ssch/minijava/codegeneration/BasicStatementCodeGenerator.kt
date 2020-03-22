package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.grammar.MiniJavaParser

class BasicStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.ExpressionStmtContext) {
        if (ctx.expr() is MiniJavaParser.CallExprContext) {
            val exprType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

            if (exprType != null) {
                codeGenerationPhase.currentFunction.body.instructions.add(Instruction.drop)
            }
        } else {
            TODO()
        }
    }

    fun generateExecution(ctx: MiniJavaParser.ReturnStmtContext) {
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.value)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction._return)
    }

    fun generateExecution(ctx: MiniJavaParser.BlockStmtContext) {
        codeGenerationPhase.localsVariableSymbolTable.pushScope()
        ctx.statements.forEach(codeGenerationPhase.statementCodeGenerator::generateExecution)
        codeGenerationPhase.localsVariableSymbolTable.popScope()
    }
}