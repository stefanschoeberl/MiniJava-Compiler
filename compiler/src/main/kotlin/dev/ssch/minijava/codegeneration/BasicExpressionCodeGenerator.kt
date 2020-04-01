package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.castTypeTo
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser

class BasicExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.IdExprContext): DataType {
        val name = ctx.IDENT().text

        val instructions = codeGenerationPhase.currentFunction.body.instructions
        val localsVariableSymbolTable = codeGenerationPhase.localsVariableSymbolTable

        return when {
            localsVariableSymbolTable.isDeclared(name) -> {
                instructions.add(Instruction.local_get(localsVariableSymbolTable.addressOf(name)))
                localsVariableSymbolTable.typeOf(name)
            }
            localsVariableSymbolTable.doesThisParameterExist() -> {
                codeGenerationPhase.memberAccessExpressionCodeGenerator.generateEvaluation(name, ctx.start, this::generateThis)
            }
            else -> throw UndefinedVariableException(name, ctx.IDENT().symbol)
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

    fun generateEvaluation(ctx: MiniJavaParser.CharExprContext): DataType {
        val char = ctx.CHAR().text[1]
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(char.toInt()))
        return DataType.PrimitiveType.Char
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

    fun generateNullEvaluation(): DataType {
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(0))
        return DataType.NullType
    }

    fun generateThisEvaluation(ctx: MiniJavaParser.ThisExprContext): DataType? {
        return if (codeGenerationPhase.localsVariableSymbolTable.doesThisParameterExist()) {
            generateThis()
        } else {
            throw ThisReferencedFromStaticContextException(ctx.start)
        }
    }

    private fun generateThis(): DataType {
        val thisAddress = codeGenerationPhase.localsVariableSymbolTable.addressOfThis()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_get(thisAddress))
        return DataType.ReferenceType(codeGenerationPhase.currentClass)
    }
}