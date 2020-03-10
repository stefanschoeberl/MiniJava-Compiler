package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.castTypeTo
import dev.ssch.minijava.exception.InconvertibleTypeException
import dev.ssch.minijava.exception.UnknownTypeException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser

class BasicStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.ExpressionStmtContext) {
        if (ctx.expr() is MiniJavaParser.CallExprContext) {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

            if (ctx.expr().staticType != null) {
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
        codeGenerationPhase.visitChildren(ctx)
        codeGenerationPhase.localsVariableSymbolTable.popScope()
    }
}