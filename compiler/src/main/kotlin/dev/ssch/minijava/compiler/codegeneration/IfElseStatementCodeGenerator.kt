package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class IfElseStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

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
        val conditionType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(condition)
        if (conditionType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, conditionType, condition.getStart())
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