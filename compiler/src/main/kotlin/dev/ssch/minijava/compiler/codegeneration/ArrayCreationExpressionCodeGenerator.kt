package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.BuiltinFunctions
import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class ArrayCreationExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val builtinFunctions: BuiltinFunctions
) {

    fun generateEvaluation(ctx: MiniJavaParser.ArrayCreationExprContext): DataType {
        val sizeType = expressionCodeGenerator.generateEvaluation(ctx.size)
        if (sizeType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, sizeType, ctx.size.start)
        }
        val arrayType = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(codeEmitter.classSymbolTable)
            ?: TODO()

        val address = when (arrayType) {
            is DataType.PrimitiveType.Boolean -> builtinFunctions.newArrayBooleanAddress
            is DataType.PrimitiveType.Char -> builtinFunctions.newArrayCharAddress
            is DataType.PrimitiveType -> builtinFunctions.newArrayNumericAddress
            is DataType.ReferenceType -> builtinFunctions.newArrayReferenceAddress
            else -> TODO()
        }

        codeEmitter.emitInstruction(Instruction.call(address))

        return DataType.Array(arrayType)
    }
}