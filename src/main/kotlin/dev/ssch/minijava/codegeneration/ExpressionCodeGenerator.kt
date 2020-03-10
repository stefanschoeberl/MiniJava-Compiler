package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class ExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    private val basicExpressionCodeGenerator = BasicExpressionCodeGenerator(codeGenerationPhase)
    private val binaryExpressionCodeGenerator = BinaryExpressionCodeGenerator(codeGenerationPhase)
    private val arrayCreationExpressionCodeGenerator = ArrayCreationExpressionCodeGenerator(codeGenerationPhase)
    private val callExpressionCodeGenerator = CallExpressionCodeGeneration(codeGenerationPhase)
    private val classInstanceCreationExpressionCodeGenerator = ClassInstanceCreationExpressionCodeGenerator(codeGenerationPhase)

    private val visitor = Visitor()

    fun generateEvaluation(ctx: MiniJavaParser.ExprContext) {
        visitor.visit(ctx)
    }

    inner class Visitor : MiniJavaBaseVisitor<Unit>() {
        override fun visitArrayAccessExpr(ctx: MiniJavaParser.ArrayAccessExprContext) {
            codeGenerationPhase.arrayAccessExpressionCodeGeneration.generateEvaluation(ctx)
        }

        override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext) {
            callExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitMemberExpr(ctx: MiniJavaParser.MemberExprContext) {
            codeGenerationPhase.memberAccessExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitClassInstanceCreationExpr(ctx: MiniJavaParser.ClassInstanceCreationExprContext) {
            classInstanceCreationExpressionCodeGenerator.generate(ctx)
        }

        override fun visitArrayCreationExpr(ctx: MiniJavaParser.ArrayCreationExprContext) {
            arrayCreationExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext) {
            basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
            binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }
    }
}