package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.BuiltinFunctions
import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.assignTypeTo
import dev.ssch.minijava.compiler.exception.*
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction
import org.antlr.v4.runtime.Token

class VariableAssignmentStatementCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val arrayAccessExpressionCodeGenerator: ArrayAccessExpressionCodeGenerator,
    private val builtinFunctions: BuiltinFunctions
) {

    private fun checkAndConvertAssigment(leftType: DataType?, rightType: DataType?, token: Token) {
        val conversionCode = leftType?.let {
            rightType?.assignTypeTo(it)
        } ?: throw IncompatibleAssignmentException(leftType, rightType, token)

        codeEmitter.emitInstructions(conversionCode)
    }

    fun generateExecution(ctx: MiniJavaParser.VarassignStmtContext) {
        when (val left = ctx.left) {
            is MiniJavaParser.IdExprContext -> {
                val name = left.IDENT().text

                val localsVariableSymbolTable = codeEmitter.localsVariableSymbolTable

                when {
                    localsVariableSymbolTable.isDeclared(name) -> {
                        val rightType = expressionCodeGenerator.generateEvaluation(ctx.right)
                        checkAndConvertAssigment(codeEmitter.localsVariableSymbolTable.typeOf(name), rightType, ctx.left.start)
                        codeEmitter.emitInstruction(Instruction.local_set(codeEmitter.localsVariableSymbolTable.addressOf(name)))
                    }
                    localsVariableSymbolTable.doesThisParameterExist() -> {
                        generateWrite(name, ctx.right, ctx.left.start) {
                            codeEmitter.emitInstruction(Instruction.local_get(localsVariableSymbolTable.addressOfThis()))
                            DataType.ReferenceType(codeEmitter.currentClass)
                        }
                    }
                    else -> throw UndefinedVariableException(name, left.IDENT().symbol)

                }
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = arrayAccessExpressionCodeGenerator.generateArrayAndIndexAddressesAndReturnElementType(left)

                val rightType = expressionCodeGenerator.generateEvaluation(ctx.right)
                checkAndConvertAssigment(elementType, rightType, ctx.left.start)

                val address = when (elementType) {
                    DataType.PrimitiveType.Integer -> builtinFunctions.setArrayPrimitiveIntAddress
                    DataType.PrimitiveType.Float -> builtinFunctions.setArrayPrimitiveFloatAddress
                    DataType.PrimitiveType.Boolean -> builtinFunctions.setArrayPrimitiveBooleanAddress
                    DataType.PrimitiveType.Char -> builtinFunctions.setArrayPrimitiveCharAddress
                    is DataType.ReferenceType -> builtinFunctions.setArrayReferenceAddress
                    else -> throw IllegalStateException("Address for array storage of type $elementType does not exist")
                }

                codeEmitter.emitInstruction(Instruction.call(address))
            }
            is MiniJavaParser.MemberExprContext -> {
                generateWrite(left, ctx.right)
            }
            else -> throw InvalidAssignmentException(left.start)
        }
    }

    private fun generateWrite(ctx: MiniJavaParser.MemberExprContext, right: MiniJavaParser.ExprContext) {
        val fieldName = ctx.right.text
        generateWrite(fieldName, right, ctx.right) {
            expressionCodeGenerator.generateEvaluation(ctx.left)
        }
    }

    private fun generateWrite(fieldName: String, right: MiniJavaParser.ExprContext, token: Token, objectAddressCode: () -> DataType?) {
        val objRawType = objectAddressCode()
        val objType = objRawType as? DataType.ReferenceType
            ?: throw NotAReferenceTypeException(objRawType, token)

        val field = codeEmitter.classSymbolTable
            .getFieldSymbolTable(objType.name)
            .findFieldInfo(fieldName) ?: throw UndefinedFieldException("${objType.name}.$fieldName", token)

        val rightType = expressionCodeGenerator.generateEvaluation(right)
        checkAndConvertAssigment(field.type, rightType, token)
        codeEmitter.emitInstruction(Instruction.call(field.setterAddress))
    }
}