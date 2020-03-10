package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser

class IfElseStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, {
            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.thenbranch)
        }, {
            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.elsebranch)
        })
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        generateIfElse(ctx.condition, {
            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.thenbranch)
        })
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, {
            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.thenbranch)
        }, {
            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.elsebranch)
        })
    }

    private fun generateIfElse(condition: MiniJavaParser.ExprContext, thenbranch: () -> Unit, elsebranch: (() -> Unit)? = null) {
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(condition)
        if (condition.staticType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, condition.staticType, condition.getStart())
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction._if)
        thenbranch()
        if (elsebranch != null) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction._else)
            elsebranch()
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.end)
    }
}