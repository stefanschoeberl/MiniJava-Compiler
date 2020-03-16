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

        val size = codeGenerationPhase.classSymbolTable.getFieldSymbolTable(type.name).getSize()
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(size))

        // allocate memory
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(codeGenerationPhase.mallocAddress))

        // evaluate constructor parameters
        val parameterTypes = ctx.parameters.map(codeGenerationPhase.expressionCodeGenerator::generateEvaluation)
        val parameters = parameterTypes.mapIndexed { index, type ->
            type ?: throw VoidParameterException(ctx.parameters[index].start)
        }

        val constructors = codeGenerationPhase.classSymbolTable.getConstructorSymbolTable(type.name)

        if (constructors.isDeclared(parameters)) {
            val constructorAddress = constructors.addressOf(parameters)
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(constructorAddress))
        } else if (parameters.isNotEmpty()) {
            TODO("undeclared constructor")
        }

        return type
    }
}