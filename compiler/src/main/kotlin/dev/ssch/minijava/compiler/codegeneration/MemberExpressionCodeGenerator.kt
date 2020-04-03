package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.UndefinedVariableException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction
import org.antlr.v4.runtime.Token

class MemberExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator
) {

    fun generateEvaluation(ctx: MiniJavaParser.MemberExprContext): DataType {
        val fieldName = ctx.right.text
        return generateEvaluation(fieldName, ctx.right) {
            expressionCodeGenerator.generateEvaluation(ctx.left)
        }
    }

    fun generateEvaluation(fieldName: String, token: Token, objectAddressCode: () -> DataType?): DataType {
        val objType = objectAddressCode() as? DataType.ReferenceType ?: TODO()


        val field = codeEmitter.classSymbolTable
            .getFieldSymbolTable(objType.name)
            .findFieldInfo(fieldName) ?: throw UndefinedVariableException(fieldName, token) // TODO: better exception like "UndefinedField"

        codeEmitter.emitInstruction(Instruction.call(field.getterAddress))
        return field.type
    }


}