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
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.ADD) to BinaryOperation(null, null, Instruction.i32_add(), DataType.Integer),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.SUB) to BinaryOperation(null, null, Instruction.i32_sub(), DataType.Integer),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.MUL) to BinaryOperation(null, null, Instruction.i32_mul(), DataType.Integer),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.DIV) to BinaryOperation(null, null, Instruction.i32_div_s(), DataType.Integer),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.LT) to BinaryOperation(null, null, Instruction.i32_lt_s(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.LE) to BinaryOperation(null, null, Instruction.i32_le_s(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.GT) to BinaryOperation(null, null, Instruction.i32_gt_s(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.GE) to BinaryOperation(null, null, Instruction.i32_ge_s(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.i32_eq(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Integer, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.i32_ne(), DataType.Boolean),

        Triple(DataType.Float, DataType.Float, MiniJavaParser.ADD) to BinaryOperation(null, null, Instruction.f32_add(), DataType.Float),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.SUB) to BinaryOperation(null, null, Instruction.f32_sub(), DataType.Float),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.MUL) to BinaryOperation(null, null, Instruction.f32_mul(), DataType.Float),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.DIV) to BinaryOperation(null, null, Instruction.f32_div(), DataType.Float),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.LT) to BinaryOperation(null, null, Instruction.f32_lt(), DataType.Boolean),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.LE) to BinaryOperation(null, null, Instruction.f32_le(), DataType.Boolean),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.GT) to BinaryOperation(null, null, Instruction.f32_gt(), DataType.Boolean),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.GE) to BinaryOperation(null, null, Instruction.f32_ge(), DataType.Boolean),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.f32_eq(), DataType.Boolean),
        Triple(DataType.Float, DataType.Float, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.f32_ne(), DataType.Boolean),

        Triple(DataType.Float, DataType.Integer, MiniJavaParser.ADD) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_add(), DataType.Float),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.SUB) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_sub(), DataType.Float),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.MUL) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_mul(), DataType.Float),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.DIV) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_div(), DataType.Float),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.LT) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_lt(), DataType.Boolean),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.LE) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_le(), DataType.Boolean),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.GT) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_gt(), DataType.Boolean),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.GE) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_ge(), DataType.Boolean),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.EQ) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_eq(), DataType.Boolean),
        Triple(DataType.Float, DataType.Integer, MiniJavaParser.NEQ) to BinaryOperation(null, Instruction.f32_convert_i32_s(), Instruction.f32_ne(), DataType.Boolean),

        Triple(DataType.Integer, DataType.Float, MiniJavaParser.ADD) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_add(), DataType.Float),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.SUB) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_sub(), DataType.Float),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.MUL) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_mul(), DataType.Float),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.DIV) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_div(), DataType.Float),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.LT) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_lt(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.LE) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_le(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.GT) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_gt(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.GE) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_ge(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.EQ) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_eq(), DataType.Boolean),
        Triple(DataType.Integer, DataType.Float, MiniJavaParser.NEQ) to BinaryOperation(Instruction.f32_convert_i32_s(), null, Instruction.f32_ne(), DataType.Boolean),

        Triple(DataType.Boolean, DataType.Boolean, MiniJavaParser.EQ) to BinaryOperation(null, null, Instruction.i32_eq(), DataType.Boolean),
        Triple(DataType.Boolean, DataType.Boolean, MiniJavaParser.NEQ) to BinaryOperation(null, null, Instruction.i32_ne(), DataType.Boolean),
        Triple(DataType.Boolean, DataType.Boolean, MiniJavaParser.AND) to BinaryOperation(null, null, Instruction.i32_and(), DataType.Boolean),
        Triple(DataType.Boolean, DataType.Boolean, MiniJavaParser.OR) to BinaryOperation(null, null, Instruction.i32_or(), DataType.Boolean)
    )

    fun findBinaryOperation(left: DataType?, right: DataType?, op: Token): BinaryOperation? {
        return binaryOperations[Triple(left, right, op.type)]
    }

    fun findUnaryMinusOperation(type: DataType?): UnaryOperation? {
        return when (type) {
            DataType.Integer -> UnaryOperation(Instruction.i32_const(0), Instruction.i32_sub())
            DataType.Float -> UnaryOperation(Instruction.f32_const(0f), Instruction.f32_sub())
            else -> null
        }
    }
}