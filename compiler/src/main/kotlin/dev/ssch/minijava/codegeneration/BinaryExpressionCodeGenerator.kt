package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.exception.InvalidBinaryOperationException
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
        val codePositionAfterLeftOperand = codeGenerationPhase.currentFunction.body.instructions.size
        val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(right)

        val binaryOperation = codeGenerationPhase.operatorTable.findBinaryOperation(leftType, rightType, op)
            ?: throw InvalidBinaryOperationException(leftType, rightType, op)

        binaryOperation.leftPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(codePositionAfterLeftOperand, it) }
        binaryOperation.rightPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(it) }

        codeGenerationPhase.currentFunction.body.instructions.add(binaryOperation.operation)

        return binaryOperation.resultingType
    }
}