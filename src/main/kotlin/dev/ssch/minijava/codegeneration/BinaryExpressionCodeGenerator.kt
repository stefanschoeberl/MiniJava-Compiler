package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class BinaryExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase)  {

    fun generateEvaluation(ctx: MiniJavaParser.OrExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.AndExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.EqNeqExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.RelationalExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.AddSubExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEvaluation(ctx: MiniJavaParser.MulDivExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    private fun visitBinaryOperatorExpression(ctx: MiniJavaParser.ExprContext, left: MiniJavaParser.ExprContext, right: MiniJavaParser.ExprContext, op: Token) {
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(left)
        val codePositionAfterLeftOperand = codeGenerationPhase.currentFunction.body.instructions.size
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(right)

        val binaryOperation = codeGenerationPhase.operatorTable.findBinaryOperation(left.staticType, right.staticType, op)
            ?: throw InvalidBinaryOperationException(left.staticType, right.staticType, op)

        binaryOperation.leftPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(codePositionAfterLeftOperand, it) }
        binaryOperation.rightPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(it) }

        codeGenerationPhase.currentFunction.body.instructions.add(binaryOperation.operation)

        ctx.staticType = binaryOperation.resultingType
    }
}