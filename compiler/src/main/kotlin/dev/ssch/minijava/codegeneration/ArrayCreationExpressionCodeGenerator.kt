package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class ArrayCreationExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.ArrayCreationExprContext): DataType {
        val sizeType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.size)
        if (sizeType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, sizeType, ctx.size.start)
        }
        val arrayType = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(codeGenerationPhase.classSymbolTable)
            ?: TODO()

        val address = when (arrayType) {
            is DataType.PrimitiveType.Boolean -> codeGenerationPhase.newArrayBooleanAddress
            is DataType.PrimitiveType.Char -> codeGenerationPhase.newArrayCharAddress
            is DataType.PrimitiveType -> codeGenerationPhase.newArrayNumericAddress
            is DataType.ReferenceType -> codeGenerationPhase.newArrayReferenceAddress
            else -> TODO()
        }

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(address))

        return DataType.Array(arrayType)
    }
}