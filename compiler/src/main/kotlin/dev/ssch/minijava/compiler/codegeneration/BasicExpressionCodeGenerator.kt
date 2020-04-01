package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.castTypeTo
import dev.ssch.minijava.compiler.exception.*
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class BasicExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.IdExprContext): DataType {
        val name = ctx.IDENT().text

        val localsVariableSymbolTable = codeGenerationPhase.localsVariableSymbolTable

        return when {
            localsVariableSymbolTable.isDeclared(name) -> {
                codeGenerationPhase.emitInstruction(Instruction.local_get(localsVariableSymbolTable.addressOf(name)))
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
        codeGenerationPhase.emitInstruction(Instruction.i32_const(value))
        return DataType.PrimitiveType.Integer
    }

    fun generateEvaluation(ctx: MiniJavaParser.BoolExprContext): DataType {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            codeGenerationPhase.emitInstruction(Instruction.i32_const(1))
        } else {
            codeGenerationPhase.emitInstruction(Instruction.i32_const(0))
        }
        return DataType.PrimitiveType.Boolean
    }

    fun generateEvaluation(ctx: MiniJavaParser.FloatExprContext): DataType {
        val value = ctx.FLOAT().text.toFloat()
        codeGenerationPhase.emitInstruction(Instruction.f32_const(value))
        return DataType.PrimitiveType.Float
    }

    fun generateEvaluation(ctx: MiniJavaParser.CharExprContext): DataType {
        val char = ctx.CHAR().text[1]
        codeGenerationPhase.emitInstruction(Instruction.i32_const(char.toInt()))
        return DataType.PrimitiveType.Char
    }

    fun generateEvaluation(ctx: MiniJavaParser.ParensExprContext): DataType? {
        return codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())
    }

    fun generateEvaluation(ctx: MiniJavaParser.MinusExprContext): DataType? {
        val codePositionBeforeOperand = codeGenerationPhase.nextInstructionAddress
        val type = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

        val unaryOperation = codeGenerationPhase.operatorTable.findUnaryMinusOperation(type)
            ?: throw InvalidUnaryOperationException(type, ctx.SUB().symbol)

        codeGenerationPhase.emitInstruction(codePositionBeforeOperand, unaryOperation.operationBeforeOperand)
        codeGenerationPhase.emitInstruction(unaryOperation.operationAfterOperand)

        return type
    }

    fun generateEvaluation(ctx: MiniJavaParser.CastExprContext): DataType {
        val type = ctx.type.getDataType(codeGenerationPhase.classSymbolTable) ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

        val exprType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

        val castCode = exprType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(exprType, type, ctx.start)

        codeGenerationPhase.emitInstructions(castCode)

        return type
    }

    fun generateNullEvaluation(): DataType {
        codeGenerationPhase.emitInstruction(Instruction.i32_const(0))
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
        codeGenerationPhase.emitInstruction(Instruction.local_get(thisAddress))
        return DataType.ReferenceType(codeGenerationPhase.currentClass)
    }
}