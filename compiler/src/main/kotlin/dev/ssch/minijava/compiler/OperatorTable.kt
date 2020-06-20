package dev.ssch.minijava.compiler

import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction
import org.antlr.v4.runtime.Token

class OperatorTable (
    private val builtinFunctions: BuiltinFunctions
) {

    data class BinaryOperation (
        val leftPromotion: Instruction?,
        val rightPromotion: Instruction?,
        val operation: Instruction,
        val resultingType: DataType
    )

    data class UnaryOperation(
        val operationBeforeOperand: Instruction?,
        val operationAfterOperand: Instruction
    )

    private val numericTypes = hashSetOf(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float, DataType.PrimitiveType.Char)
    private val floatPromotions = hashMapOf(
        DataType.PrimitiveType.Integer to Instruction.f32_convert_i32_s,
        DataType.PrimitiveType.Char to Instruction.f32_convert_i32_s
    )

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

    private val charOperations = hashMapOf(
        MiniJavaParser.ADD to Pair(Instruction.i32_add, DataType.PrimitiveType.Char),
        MiniJavaParser.SUB to Pair(Instruction.i32_sub, DataType.PrimitiveType.Char),
        MiniJavaParser.MUL to Pair(Instruction.i32_mul, DataType.PrimitiveType.Char),
        MiniJavaParser.DIV to Pair(Instruction.i32_div_s, DataType.PrimitiveType.Char),
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

    private val referenceOperations = hashMapOf(
        MiniJavaParser.EQ to Pair(Instruction.i32_eq, DataType.PrimitiveType.Boolean),
        MiniJavaParser.NEQ to Pair(Instruction.i32_ne, DataType.PrimitiveType.Boolean)
    )

    private val stringOperations = hashMapOf(
        Pair(DataType.ReferenceType.StringType, DataType.ReferenceType.StringType) to { builtinFunctions.concatStringStringAddress },
        Pair(DataType.ReferenceType.StringType, DataType.PrimitiveType.Integer) to { builtinFunctions.concatStringIntAddress },
        Pair(DataType.PrimitiveType.Integer, DataType.ReferenceType.StringType) to { builtinFunctions.concatIntStringAddress },
        Pair(DataType.ReferenceType.StringType, DataType.PrimitiveType.Float) to { builtinFunctions.concatStringFloatAddress },
        Pair(DataType.PrimitiveType.Float, DataType.ReferenceType.StringType) to { builtinFunctions.concatFloatStringAddress },
        Pair(DataType.ReferenceType.StringType, DataType.PrimitiveType.Boolean) to { builtinFunctions.concatStringBooleanAddress },
        Pair(DataType.PrimitiveType.Boolean, DataType.ReferenceType.StringType) to { builtinFunctions.concatBooleanStringAddress },
        Pair(DataType.ReferenceType.StringType, DataType.PrimitiveType.Char) to { builtinFunctions.concatStringCharAddress },
        Pair(DataType.PrimitiveType.Char, DataType.ReferenceType.StringType) to { builtinFunctions.concatCharStringAddress },
        Pair(DataType.ReferenceType.StringType, DataType.NullType) to { builtinFunctions.concatStringReferenceAddress },
        Pair(DataType.NullType, DataType.ReferenceType.StringType) to { builtinFunctions.concatReferenceStringAddress }
    )

    fun findBinaryOperation(left: DataType?, right: DataType?, op: Token): BinaryOperation? {
        return if (left == DataType.ReferenceType.StringType || right == DataType.ReferenceType.StringType) {
            val predefinedOperationAddress = stringOperations[Pair(left, right)]
            val instructionAddress = when {
                predefinedOperationAddress != null -> predefinedOperationAddress()
                left != DataType.ReferenceType.StringType -> builtinFunctions.concatReferenceStringAddress
                else -> builtinFunctions.concatStringReferenceAddress
            }
            BinaryOperation(null, null, Instruction.call(instructionAddress), DataType.ReferenceType.StringType)
        } else if (numericTypes.contains(left) && numericTypes.contains(right)) {
            if (left == DataType.PrimitiveType.Float || right == DataType.PrimitiveType.Float) {
                floatOperations[op.type]?.let {
                    BinaryOperation(floatPromotions[left], floatPromotions[right], it.first, it.second)
                }
            } else if (left == DataType.PrimitiveType.Integer || right == DataType.PrimitiveType.Integer) {
                intOperations[op.type]?.let {
                    BinaryOperation(null, null, it.first, it.second)
                }
            } else {
                charOperations[op.type]?.let {
                    BinaryOperation(null, null, it.first, it.second)
                }
            }
        }
        else if (left is DataType.ReferenceType && (left == right || right == DataType.NullType)
            || left == DataType.NullType && (right is DataType.ReferenceType || right == DataType.NullType)) {
            referenceOperations[op.type]?.let {
                BinaryOperation(null, null, it.first, it.second)
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
            DataType.PrimitiveType.Float -> UnaryOperation(null, Instruction.f32_neg)
            else -> null
        }
    }
}