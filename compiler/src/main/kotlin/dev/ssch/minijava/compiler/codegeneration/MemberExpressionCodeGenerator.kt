package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.BuiltinFunctions
import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.NotAReferenceTypeException
import dev.ssch.minijava.compiler.exception.UndefinedFieldException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction
import org.antlr.v4.runtime.Token

class MemberExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val builtinFunctions: BuiltinFunctions
) {

    fun generateEvaluation(ctx: MiniJavaParser.MemberExprContext): DataType {
        val fieldName = ctx.right.text
        return generateEvaluation(fieldName, ctx.right) {
            expressionCodeGenerator.generateEvaluation(ctx.left)
        }
    }

    fun generateEvaluation(fieldName: String, token: Token, objectAddressCode: () -> DataType?): DataType {
        val objRawType = objectAddressCode()
        if (fieldName == "length" && objRawType is DataType.Array) {
            return evaluateArrayLength()
        } else {
            return evaluateField(fieldName, objRawType, token)
        }
    }

    private fun evaluateField(fieldName: String, objRawType: DataType?, token: Token): DataType {
        val objType = objRawType as? DataType.ReferenceType
            ?: throw NotAReferenceTypeException(objRawType, token)

        val field = codeEmitter.classSymbolTable
            .getFieldSymbolTable(objType.name)
            .findFieldInfo(fieldName) ?: throw UndefinedFieldException(fieldName, token)

        codeEmitter.emitInstruction(Instruction.call(field.getterAddress))
        return field.type
    }

    private fun evaluateArrayLength(): DataType {
        codeEmitter.emitInstruction(Instruction.call(builtinFunctions.arrayLengthAddress))
        return DataType.PrimitiveType.Integer
    }
}