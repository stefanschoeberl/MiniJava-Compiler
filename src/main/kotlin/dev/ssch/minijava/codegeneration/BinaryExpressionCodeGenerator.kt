package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class BinaryExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase)  {

    fun generateOrExpr(ctx: MiniJavaParser.OrExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateAndExpr(ctx: MiniJavaParser.AndExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    fun generateMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    private fun visitBinaryOperatorExpression(ctx: MiniJavaParser.ExprContext, left: MiniJavaParser.ExprContext, right: MiniJavaParser.ExprContext, op: Token) {
        codeGenerationPhase.visit(left)
        val codePositionAfterLeftOperand = codeGenerationPhase.currentFunction.body.instructions.size
        codeGenerationPhase.visit(right)

        val binaryOperation = codeGenerationPhase.operatorTable.findBinaryOperation(left.staticType, right.staticType, op)
            ?: throw InvalidBinaryOperationException(left.staticType, right.staticType, op)

        binaryOperation.leftPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(codePositionAfterLeftOperand, it) }
        binaryOperation.rightPromotion?.let { codeGenerationPhase.currentFunction.body.instructions.add(it) }

        codeGenerationPhase.currentFunction.body.instructions.add(binaryOperation.operation)

        ctx.staticType = binaryOperation.resultingType
    }
}