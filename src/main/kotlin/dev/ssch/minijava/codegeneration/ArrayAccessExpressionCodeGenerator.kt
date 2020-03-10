package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.ExpressionIsNotAnArrayException
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.getLoadMemoryInstruction
import dev.ssch.minijava.grammar.MiniJavaParser

class ArrayAccessExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateElementAddressCodeAndReturnElementType(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        // address = arraystart + itemsize * index + 4
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.array)
        val arrayType = ctx.array.staticType as? DataType.Array ?: throw ExpressionIsNotAnArrayException(ctx.array.start)
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.index)
        if (ctx.index.staticType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, ctx.index.staticType, ctx.index.start)
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(arrayType.elementType.sizeInBytes()))

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_mul)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        // skip size part
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(4))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        return arrayType.elementType
    }

    fun generateEvaluation(ctx: MiniJavaParser.ArrayAccessExprContext) {
        val elementType = generateElementAddressCodeAndReturnElementType(ctx)
        codeGenerationPhase.currentFunction.body.instructions.add(elementType.getLoadMemoryInstruction())
        ctx.staticType = elementType
    }
}