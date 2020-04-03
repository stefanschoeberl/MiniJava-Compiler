package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.VoidParameterException
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class ClassInstanceCreationExpressionCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator
) {

    fun generate(ctx: MiniJavaParser.ClassInstanceCreationExprContext): DataType {
        val type = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(codeEmitter.classSymbolTable)
                as? DataType.ReferenceType // TODO
            ?: TODO()

        val constructorAddress = codeEmitter.classSymbolTable.getConstructorAddress(type.name)
        codeEmitter.emitInstruction(Instruction.call(constructorAddress))

        // evaluate initializer parameters
        val parameterTypes = ctx.parameters.map(expressionCodeGenerator::generateEvaluation)
        val parameters = parameterTypes.mapIndexed { index, parameterType ->
            parameterType ?: throw VoidParameterException(ctx.parameters[index].start)
        }

        val initializers = codeEmitter.classSymbolTable.getInitializerSymbolTable(type.name)

        if (initializers.isDeclared(parameters)) {
            val initializerAddress = initializers.addressOf(parameters)
            codeEmitter.emitInstruction(Instruction.call(initializerAddress))
        } else if (parameters.isNotEmpty()) {
            TODO("undeclared initializer")
        }

        return type
    }
}