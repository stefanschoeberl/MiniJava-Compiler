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

    private val numericTypes = hashSetOf(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float)
    private val floatPromotions = hashMapOf(DataType.PrimitiveType.Integer to Instruction.f32_convert_i32_s)

    private val intOperations = hashMapOf(
        MiniJavaParser.ADD to Pair(Instruction.i32_add, DataType.PrimitiveType.Integer),
        MiniJavaParser.SUB to Pair(Instruction.i32_sub, DataType.PrimitiveType.Integer),
        MiniJavaParser.MUL to Pair(Instruction.i32_mul, DataType.PrimitiveType.Integer),
        MiniJavaParser.DIV to Pair(Instruction.i32_div_s, DataType.PrimitiveType.Integer),
        MiniJavaParser.LT to Pair(Instruction.i32_lt_s, DataType.PrimitiveType.Boolean),
        MiniJavaParser.LE to Pair(Instruction.i32_le_s, DataType.PrimitiveType.Boolean),
        MiniJavaParser.GT to Pair(Instruction.i32_gt_s, DataType.PrimitiveType.Boolean),
        MiniJavaParser.GE to Pair(Instruction.i32_ge_s, DataType.PrimitiveType.Boolean),
        MiniJavaParser.EQ to Pair(Instruction.i32_eq, DataType.PrimitiveType.Boolean),
        MiniJavaParser.NEQ to Pair(Instruction.i32_ne, DataType.PrimitiveType.Boolean)
    )

    private val floatOperations = hashMapOf(
        MiniJavaParser.ADD to Pair(Instruction.f32_add, DataType.PrimitiveType.Float),
        MiniJavaParser.SUB to Pair(Instruction.f32_sub, DataType.PrimitiveType.Float),
        MiniJavaParser.MUL to Pair(Instruction.f32_mul, DataType.PrimitiveType.Float),
        MiniJavaParser.DIV to Pair(Instruction.f32_div, DataType.PrimitiveType.Float),
        MiniJavaParser.LT to Pair(Instruction.f32_lt, DataType.PrimitiveType.Boolean),
        MiniJavaParser.LE to Pair(Instruction.f32_le, DataType.PrimitiveType.Boolean),
        MiniJavaParser.GT to Pair(Instruction.f32_gt, DataType.PrimitiveType.Boolean),
        MiniJavaParser.GE to Pair(Instruction.f32_ge, DataType.PrimitiveType.Boolean),
        MiniJavaParser.EQ to Pair(Instruction.f32_eq, DataType.PrimitiveType.Boolean),
        MiniJavaParser.NEQ to Pair(Instruction.f32_ne, DataType.PrimitiveType.Boolean)
    )

    private val booleanOperations = hashMapOf(
        MiniJavaParser.EQ to Pair(Instruction.i32_eq, DataType.PrimitiveType.Boolean),
        MiniJavaParser.NEQ to Pair(Instruction.i32_ne, DataType.PrimitiveType.Boolean),
        MiniJavaParser.AND to Pair(Instruction.i32_and, DataType.PrimitiveType.Boolean),
        MiniJavaParser.OR to Pair(Instruction.i32_or, DataType.PrimitiveType.Boolean)
    )

    fun findBinaryOperation(left: DataType?, right: DataType?, op: Token): BinaryOperation? {
        return if (numericTypes.contains(left) && numericTypes.contains(right)) {
            if (left == DataType.PrimitiveType.Float || right == DataType.PrimitiveType.Float) {
                floatOperations[op.type]?.let {
                    BinaryOperation(floatPromotions[left], floatPromotions[right], it.first, it.second)
                }
            } else {
                intOperations[op.type]?.let {
                    BinaryOperation(null, null, it.first, it.second)
                }
            }
        }
        else if (left == DataType.PrimitiveType.Boolean && right == DataType.PrimitiveType.Boolean) {
            booleanOperations[op.type]?.let {
                BinaryOperation(null, null, it.first, it.second)
            }
        }
        else {
            null
        }
    }

    fun findUnaryMinusOperation(type: DataType?): UnaryOperation? {
        return when (type) {
            DataType.PrimitiveType.Integer -> UnaryOperation(Instruction.i32_const(0), Instruction.i32_sub)
            DataType.PrimitiveType.Float -> UnaryOperation(Instruction.f32_const(0f), Instruction.f32_sub)
            else -> null
        }
    }
}