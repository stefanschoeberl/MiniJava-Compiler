package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class ArrayAccessExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateArrayAndIndexAddressesAndReturnElementType(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        val arrayType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.array)
                as? DataType.Array ?: throw ExpressionIsNotAnArrayException(ctx.array.start)

        val indexType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.index)
        if (indexType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, indexType, ctx.index.start)
        }
        return arrayType.elementType
    }

    fun generateEvaluation(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        val elementType = generateArrayAndIndexAddressesAndReturnElementType(ctx)
        val address = when (elementType) {
            DataType.PrimitiveType.Integer -> codeGenerationPhase.getArrayPrimitiveIntAddress
            DataType.PrimitiveType.Float -> codeGenerationPhase.getArrayPrimitiveFloatAddress
            DataType.PrimitiveType.Boolean -> codeGenerationPhase.getArrayPrimitiveBooleanAddress
            DataType.PrimitiveType.Char -> codeGenerationPhase.getArrayPrimitiveCharAddress
            is DataType.ReferenceType -> codeGenerationPhase.getArrayReferenceAddress
            else -> TODO()
        }

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(address))
        return elementType
    }
}