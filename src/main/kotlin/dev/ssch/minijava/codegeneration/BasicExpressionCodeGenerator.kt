package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.InvalidUnaryOperationException
import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.minijava.grammar.MiniJavaParser

class BasicExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generate(ctx: MiniJavaParser.IdExprContext) {
        val name = ctx.IDENT().text
        if (!codeGenerationPhase.localsVariableSymbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_get(codeGenerationPhase.localsVariableSymbolTable.addressOf(name)))
        ctx.staticType = codeGenerationPhase.localsVariableSymbolTable.typeOf(name)
    }

    fun generate(ctx: MiniJavaParser.IntExprContext) {
        val value = ctx.INT().text.toInt()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(value))
        ctx.staticType = DataType.PrimitiveType.Integer
    }

    fun generate(ctx: MiniJavaParser.BoolExprContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(0))
        }
        ctx.staticType = DataType.PrimitiveType.Boolean
    }

    fun generate(ctx: MiniJavaParser.FloatExprContext) {
        val value = ctx.FLOAT().text.toFloat()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.f32_const(value))
        ctx.staticType = DataType.PrimitiveType.Float
    }

    fun generate(ctx: MiniJavaParser.ParensExprContext) {
        codeGenerationPhase.visit(ctx.expr())

        ctx.staticType = ctx.expr().staticType
    }

    fun generate(ctx: MiniJavaParser.MinusExprContext) {
        val codePositionBeforeOperand = codeGenerationPhase.currentFunction.body.instructions.size
        codeGenerationPhase.visit(ctx.expr())

        val type = ctx.expr().staticType
        val unaryOperation = codeGenerationPhase.operatorTable.findUnaryMinusOperation(type)
            ?: throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)

        codeGenerationPhase.currentFunction.body.instructions.add(codePositionBeforeOperand, unaryOperation.operationBeforeOperand)
        codeGenerationPhase.currentFunction.body.instructions.add(unaryOperation.operationAfterOperand)

        ctx.staticType = type
    }
}