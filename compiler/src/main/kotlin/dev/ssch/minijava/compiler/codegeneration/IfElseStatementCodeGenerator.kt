package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class IfElseStatementCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val statementCodeGenerator: StatementCodeGenerator,
    private val expressionCodeGenerator: ExpressionCodeGenerator
) {

    fun generateExecution(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, {
            statementCodeGenerator.generateExecution(ctx.thenbranch)
        }, {
            statementCodeGenerator.generateExecution(ctx.elsebranch)
        })
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        generateIfElse(ctx.condition, {
            statementCodeGenerator.generateExecution(ctx.thenbranch)
        })
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, {
            statementCodeGenerator.generateExecution(ctx.thenbranch)
        }, {
            statementCodeGenerator.generateExecution(ctx.elsebranch)
        })
    }

    private fun generateIfElse(condition: MiniJavaParser.ExprContext, thenbranch: () -> Unit, elsebranch: (() -> Unit)? = null) {
        val conditionType = expressionCodeGenerator.generateEvaluation(condition)
        if (conditionType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, conditionType, condition.getStart())
        }
        codeEmitter.emitInstruction(Instruction._if)
        thenbranch()
        if (elsebranch != null) {
            codeEmitter.emitInstruction(Instruction._else)
            elsebranch()
        }
        codeEmitter.emitInstruction(Instruction.end)
    }
}