package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.getLoadMemoryInstruction
import dev.ssch.minijava.grammar.MiniJavaParser

class ArrayAccessExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateElementAddressCodeAndReturnElementType(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        // address = arraystart + itemsize * index + 4
        val arrayType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.array)
             as? DataType.Array ?: throw ExpressionIsNotAnArrayException(ctx.array.start)
        val indexType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.index)
        if (indexType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, indexType, ctx.index.start)
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(arrayType.elementType.sizeInBytes()))

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_mul)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        // skip size part
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(4))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        return arrayType.elementType
    }

    fun generateEvaluation(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        val elementType = generateElementAddressCodeAndReturnElementType(ctx)
        codeGenerationPhase.currentFunction.body.instructions.add(elementType.getLoadMemoryInstruction())
        return elementType
    }
}