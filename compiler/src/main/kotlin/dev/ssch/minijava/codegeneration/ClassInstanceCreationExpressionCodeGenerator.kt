package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.VoidParameterException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser

class ClassInstanceCreationExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generate(ctx: MiniJavaParser.ClassInstanceCreationExprContext): DataType {
        val type = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(codeGenerationPhase.classSymbolTable)
                as? DataType.ReferenceType // TODO
            ?: TODO()

        val constructorAddress = codeGenerationPhase.classSymbolTable.getConstructorAddress(type.name)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(constructorAddress))

        // evaluate initializer parameters
        val parameterTypes = ctx.parameters.map(codeGenerationPhase.expressionCodeGenerator::generateEvaluation)
        val parameters = parameterTypes.mapIndexed { index, parameterType ->
            parameterType ?: throw VoidParameterException(ctx.parameters[index].start)
        }

        val initializers = codeGenerationPhase.classSymbolTable.getInitializerSymbolTable(type.name)

        if (initializers.isDeclared(parameters)) {
            val initializerAddress = initializers.addressOf(parameters)
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(initializerAddress))
        } else if (parameters.isNotEmpty()) {
            TODO("undeclared initializer")
        }

        return type
    }
}