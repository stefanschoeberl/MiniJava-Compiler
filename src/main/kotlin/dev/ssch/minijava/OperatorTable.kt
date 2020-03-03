package dev.ssch.minijava

import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class OperatorTable {

    data class BinaryOperation (
        val leftPromotion: Instruction?,
        val rightPromotion: Instruction?,
        val operation: Instruction,
        val resultingType: DataType
    )

    data class UnaryOperation (
        val operationBeforeOperand: Instruction,
        val operationAfterOperand: Instruction
    )

    private val binaryOperations = hashMapOf(
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.ADD) to BinaryOperation(null, null, Instruction.i32_add, DataType.PrimitiveType.Integer),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.SUB) to BinaryOperation(null, null, Instruction.i32_sub, DataType.PrimitiveType.Integer),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.MUL) to BinaryOperation(null, null, Instruction.i32_mul, DataType.PrimitiveType.Integer),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.DIV) to BinaryOperation(null, null, Instruction.i32_div_s, DataType.PrimitiveType.Integer),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.LT) to BinaryOperation(null, null, Instruction.i32_lt_s, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.LE) to BinaryOperation(null, null, Instruction.i32_le_s, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.GT) to BinaryOperation(null, null, Instruction.i32_gt_s, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.GE) to BinaryOperation(null, null, Instruction.i32_ge_s, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.i32_eq, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Integer, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.i32_ne, DataType.PrimitiveType.Boolean),

        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.ADD) to BinaryOperation(null, null, Instruction.f32_add, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.SUB) to BinaryOperation(null, null, Instruction.f32_sub, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.MUL) to BinaryOperation(null, null, Instruction.f32_mul, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.DIV) to BinaryOperation(null, null, Instruction.f32_div, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.LT) to BinaryOperation(null, null, Instruction.f32_lt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.LE) to BinaryOperation(null, null, Instruction.f32_le, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.GT) to BinaryOperation(null, null, Instruction.f32_gt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.GE) to BinaryOperation(null, null, Instruction.f32_ge, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.f32_eq, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Float, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.f32_ne, DataType.PrimitiveType.Boolean),

        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.ADD) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_add, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.SUB) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_sub, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.MUL) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_mul, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.DIV) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_div, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.LT) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_lt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.LE) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_le, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.GT) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_gt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.GE) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_ge, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.EQ) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_eq, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer, MiniJavaParser.NEQ) to BinaryOperation(null, Instruction.f32_convert_i32_s, Instruction.f32_ne, DataType.PrimitiveType.Boolean),

        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.ADD) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_add, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.SUB) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_sub, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.MUL) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_mul, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.DIV) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_div, DataType.PrimitiveType.Float),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.LT) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_lt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.LE) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_le, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.GT) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_gt, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.GE) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_ge, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.EQ) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_eq, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, MiniJavaParser.NEQ) to BinaryOperation(Instruction.f32_convert_i32_s, null, Instruction.f32_ne, DataType.PrimitiveType.Boolean),

        Triple(DataType.PrimitiveType.Boolean, DataType.PrimitiveType.Boolean, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.i32_eq, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Boolean, DataType.PrimitiveType.Boolean, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.i32_ne, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Boolean, DataType.PrimitiveType.Boolean, MiniJavaParser.AND) to BinaryOperation(null, null, Instruction.i32_and, DataType.PrimitiveType.Boolean),
        Triple(DataType.PrimitiveType.Boolean, DataType.PrimitiveType.Boolean, MiniJavaParser.OR) to BinaryOperation(null, null, Instruction.i32_or, DataType.PrimitiveType.Boolean)
    )

    fun findBinaryOperation(left: DataType?, right: DataType?, op: Token): BinaryOperation? {
        return binaryOperations[Triple(left, right, op.type)]
    }

    fun findUnaryMinusOperation(type: DataType?): UnaryOperation? {
        return when (type) {
            DataType.PrimitiveType.Integer -> UnaryOperation(Instruction.i32_const(0), Instruction.i32_sub)
            DataType.PrimitiveType.Float -> UnaryOperation(Instruction.f32_const(0f), Instruction.f32_sub)
            else -> null
        }
    }
}