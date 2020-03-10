package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
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

        return type
    }
}