package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.*
import dev.ssch.minijava.compiler.exception.*
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class BasicExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val memberExpressionCodeGenerator: MemberExpressionCodeGenerator,
    private val operatorTable: OperatorTable
) {

    fun generateEvaluation(ctx: MiniJavaParser.IdExprContext): DataType {
        val name = ctx.IDENT().text

        val localsVariableSymbolTable = codeEmitter.localsVariableSymbolTable

        return when {
            localsVariableSymbolTable.isDeclared(name) -> {
                codeEmitter.emitInstruction(Instruction.local_get(localsVariableSymbolTable.addressOf(name)))
                localsVariableSymbolTable.typeOf(name)
            }
            localsVariableSymbolTable.doesThisParameterExist() -> {
                memberExpressionCodeGenerator.generateEvaluation(name, ctx.start, this::generateThis)
            }
            else -> throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
    }

    fun generateEvaluation(ctx: MiniJavaParser.IntExprContext): DataType {
        val value = ctx.INT().text.toInt()
        codeEmitter.emitInstruction(Instruction.i32_const(value))
        return DataType.PrimitiveType.Integer
    }

    fun generateEvaluation(ctx: MiniJavaParser.BoolExprContext): DataType {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            codeEmitter.emitInstruction(Instruction.i32_const(1))
        } else {
            codeEmitter.emitInstruction(Instruction.i32_const(0))
        }
        return DataType.PrimitiveType.Boolean
    }

    fun generateEvaluation(ctx: MiniJavaParser.FloatExprContext): DataType {
        val value = ctx.FLOAT().text.toFloat()
        codeEmitter.emitInstruction(Instruction.f32_const(value))
        return DataType.PrimitiveType.Float
    }

    fun generateEvaluation(ctx: MiniJavaParser.CharExprContext): DataType {
        val char = ctx.CHAR().text[1]
        codeEmitter.emitInstruction(Instruction.i32_const(char.toInt()))
        return DataType.PrimitiveType.Char
    }

    fun generateEvaluation(ctx: MiniJavaParser.ParensExprContext): DataType? {
        return expressionCodeGenerator.generateEvaluation(ctx.expr())
    }

    fun generateEvaluation(ctx: MiniJavaParser.MinusExprContext): DataType? {
        val codePositionBeforeOperand = codeEmitter.nextInstructionAddress
        val type = expressionCodeGenerator.generateEvaluation(ctx.expr())

        val unaryOperation = operatorTable.findUnaryMinusOperation(type)
            ?: throw InvalidUnaryOperationException(type, ctx.SUB().symbol)

        codeEmitter.emitInstruction(codePositionBeforeOperand, unaryOperation.operationBeforeOperand)
        codeEmitter.emitInstruction(unaryOperation.operationAfterOperand)

        return type
    }

    fun generateEvaluation(ctx: MiniJavaParser.CastExprContext): DataType {
        val type = ctx.type.getDataType(codeEmitter.classSymbolTable) ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

        val exprType = expressionCodeGenerator.generateEvaluation(ctx.expr())

        val castCode = exprType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(exprType, type, ctx.start)

        codeEmitter.emitInstructions(castCode)

        return type
    }

    fun generateNullEvaluation(): DataType {
        codeEmitter.emitInstruction(Instruction.i32_const(0))
        return DataType.NullType
    }

    fun generateThisEvaluation(ctx: MiniJavaParser.ThisExprContext): DataType? {
        return if (codeEmitter.localsVariableSymbolTable.doesThisParameterExist()) {
            generateThis()
        } else {
            throw ThisReferencedFromStaticContextException(ctx.start)
        }
    }

    private fun generateThis(): DataType {
        val thisAddress = codeEmitter.localsVariableSymbolTable.addressOfThis()
        codeEmitter.emitInstruction(Instruction.local_get(thisAddress))
        return DataType.ReferenceType(codeEmitter.currentClass)
    }
}