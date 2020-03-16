package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.castTypeTo
import dev.ssch.minijava.exception.InconvertibleTypeException
import dev.ssch.minijava.exception.InvalidUnaryOperationException
import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.minijava.exception.UnknownTypeException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser

class BasicExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.IdExprContext): DataType {
        val name = ctx.IDENT().text

        val instructions = codeGenerationPhase.currentFunction.body.instructions
        val localsVariableSymbolTable = codeGenerationPhase.localsVariableSymbolTable

        if (localsVariableSymbolTable.isDeclared(name)) {
            instructions.add(Instruction.local_get(localsVariableSymbolTable.addressOf(name)))
            return localsVariableSymbolTable.typeOf(name)
        } else if (localsVariableSymbolTable.doesThisParameterExist()) {
            return codeGenerationPhase.memberAccessExpressionCodeGenerator.generateEvaluation(name) {
                instructions.add(Instruction.local_get(localsVariableSymbolTable.addressOfThis()))
                DataType.ReferenceType(codeGenerationPhase.currentClass)
            }
        } else {
            throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
    }

    fun generateEvaluation(ctx: MiniJavaParser.IntExprContext): DataType {
        val value = ctx.INT().text.toInt()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(value))
        return DataType.PrimitiveType.Integer
    }

    fun generateEvaluation(ctx: MiniJavaParser.BoolExprContext): DataType {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(0))
        }
        return DataType.PrimitiveType.Boolean
    }

    fun generateEvaluation(ctx: MiniJavaParser.FloatExprContext): DataType {
        val value = ctx.FLOAT().text.toFloat()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.f32_const(value))
        return DataType.PrimitiveType.Float
    }

    fun generateEvaluation(ctx: MiniJavaParser.ParensExprContext): DataType? {
        return codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())
    }

    fun generateEvaluation(ctx: MiniJavaParser.MinusExprContext): DataType? {
        val codePositionBeforeOperand = codeGenerationPhase.currentFunction.body.instructions.size
        val type = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

        val unaryOperation = codeGenerationPhase.operatorTable.findUnaryMinusOperation(type)
            ?: throw InvalidUnaryOperationException(type, ctx.SUB().symbol)

        codeGenerationPhase.currentFunction.body.instructions.add(codePositionBeforeOperand, unaryOperation.operationBeforeOperand)
        codeGenerationPhase.currentFunction.body.instructions.add(unaryOperation.operationAfterOperand)

        return type
    }

    fun generateEvaluation(ctx: MiniJavaParser.CastExprContext): DataType {
        val type = ctx.type.getDataType(codeGenerationPhase.classSymbolTable) ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

        val exprType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

        val castCode = exprType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(exprType, type, ctx.start)

        codeGenerationPhase.currentFunction.body.instructions.addAll(castCode)

        return type
    }
}