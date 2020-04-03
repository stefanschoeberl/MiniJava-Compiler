package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class ExpressionCodeGenerator {

    fun init(basicExpressionCodeGenerator: BasicExpressionCodeGenerator,
             binaryExpressionCodeGenerator: BinaryExpressionCodeGenerator,
             arrayCreationExpressionCodeGenerator: ArrayCreationExpressionCodeGenerator,
             arrayAccessExpressionCodeGeneration: ArrayAccessExpressionCodeGenerator,
             callExpressionCodeGenerator: CallExpressionCodeGeneration,
             classInstanceCreationExpressionCodeGenerator: ClassInstanceCreationExpressionCodeGenerator,
             memberExpressionCodeGenerator: MemberExpressionCodeGenerator) {
        this.basicExpressionCodeGenerator = basicExpressionCodeGenerator
        this.binaryExpressionCodeGenerator = binaryExpressionCodeGenerator
        this.arrayCreationExpressionCodeGenerator = arrayCreationExpressionCodeGenerator
        this.arrayAccessExpressionCodeGeneration = arrayAccessExpressionCodeGeneration
        this.callExpressionCodeGenerator = callExpressionCodeGenerator
        this.classInstanceCreationExpressionCodeGenerator = classInstanceCreationExpressionCodeGenerator
        this.memberExpressionCodeGenerator = memberExpressionCodeGenerator
    }

    private lateinit var basicExpressionCodeGenerator: BasicExpressionCodeGenerator
    private lateinit var binaryExpressionCodeGenerator: BinaryExpressionCodeGenerator
    private lateinit var arrayCreationExpressionCodeGenerator: ArrayCreationExpressionCodeGenerator
    private lateinit var arrayAccessExpressionCodeGeneration: ArrayAccessExpressionCodeGenerator
    private lateinit var callExpressionCodeGenerator: CallExpressionCodeGeneration
    private lateinit var classInstanceCreationExpressionCodeGenerator: ClassInstanceCreationExpressionCodeGenerator
    private lateinit var memberExpressionCodeGenerator: MemberExpressionCodeGenerator

    private val visitor = Visitor()

    fun generateEvaluation(ctx: MiniJavaParser.ExprContext): DataType? {
        return visitor.visit(ctx)
    }

    private inner class Visitor : MiniJavaBaseVisitor<DataType?>() {
        override fun visitArrayAccessExpr(ctx: MiniJavaParser.ArrayAccessExprContext): DataType? {
            return arrayAccessExpressionCodeGeneration.generateEvaluation(ctx)
        }

        override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext): DataType? {
            return callExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitMemberExpr(ctx: MiniJavaParser.MemberExprContext): DataType? {
            return memberExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitCharExpr(ctx: MiniJavaParser.CharExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitNullExpr(ctx: MiniJavaParser.NullExprContext): DataType? {
            return basicExpressionCodeGenerator.generateNullEvaluation()
        }

        override fun visitThisExpr(ctx: MiniJavaParser.ThisExprContext): DataType? {
            return basicExpressionCodeGenerator.generateThisEvaluation(ctx)
        }

        override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitClassInstanceCreationExpr(ctx: MiniJavaParser.ClassInstanceCreationExprContext): DataType? {
            return classInstanceCreationExpressionCodeGenerator.generate(ctx)
        }

        override fun visitArrayCreationExpr(ctx: MiniJavaParser.ArrayCreationExprContext): DataType? {
            return arrayCreationExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext): DataType? {
            return basicExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }

        override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext): DataType? {
            return binaryExpressionCodeGenerator.generateEvaluation(ctx)
        }
    }
}