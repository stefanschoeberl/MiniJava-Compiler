package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.VoidParameterException
import dev.ssch.minijava.compiler.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

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