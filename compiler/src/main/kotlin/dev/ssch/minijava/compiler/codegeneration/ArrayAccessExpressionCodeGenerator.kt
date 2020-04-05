package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.BuiltinFunctions
import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class ArrayAccessExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val builtinFunctions: BuiltinFunctions
) {

    fun generateArrayAndIndexAddressesAndReturnElementType(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        val arrayType = expressionCodeGenerator.generateEvaluation(ctx.array)
                as? DataType.Array ?: throw ExpressionIsNotAnArrayException(ctx.array.start)

        val indexType = expressionCodeGenerator.generateEvaluation(ctx.index)
        if (indexType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, indexType, ctx.index.start)
        }
        return arrayType.elementType
    }

    fun generateEvaluation(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        val elementType = generateArrayAndIndexAddressesAndReturnElementType(ctx)
        val address = when (elementType) {
            DataType.PrimitiveType.Integer -> builtinFunctions.getArrayPrimitiveIntAddress
            DataType.PrimitiveType.Float -> builtinFunctions.getArrayPrimitiveFloatAddress
            DataType.PrimitiveType.Boolean -> builtinFunctions.getArrayPrimitiveBooleanAddress
            DataType.PrimitiveType.Char -> builtinFunctions.getArrayPrimitiveCharAddress
            is DataType.ReferenceType -> builtinFunctions.getArrayReferenceAddress
            else -> throw IllegalStateException("Address for array access of type $elementType does not exist")
        }

        codeEmitter.emitInstruction(Instruction.call(address))
        return elementType
    }
}