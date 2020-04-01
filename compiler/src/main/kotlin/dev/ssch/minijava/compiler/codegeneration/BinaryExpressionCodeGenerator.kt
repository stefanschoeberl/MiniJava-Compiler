package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.InvalidBinaryOperationException
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class BinaryExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.OrExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.AndExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.EqNeqExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.RelationalExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.AddSubExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.MulDivExprContext): DataType {
        return visitBinaryOperatorExpression(ctx.left, ctx.right, ctx.op)
    }

    private fun visitBinaryOperatorExpression(left: MiniJavaParser.ExprContext, right: MiniJavaParser.ExprContext, op: Token): DataType {
        val leftType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(left)
        val codePositionAfterLeftOperand = codeGenerationPhase.nextInstructionAddress
        val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(right)

        val binaryOperation = codeGenerationPhase.operatorTable.findBinaryOperation(leftType, rightType, op)
            ?: throw InvalidBinaryOperationException(leftType, rightType, op)

        binaryOperation.leftPromotion?.let { codeGenerationPhase.emitInstruction(codePositionAfterLeftOperand, it) }
        binaryOperation.rightPromotion?.let { codeGenerationPhase.emitInstruction(it) }

        codeGenerationPhase.emitInstruction(binaryOperation.operation)

        return binaryOperation.resultingType
    }
}