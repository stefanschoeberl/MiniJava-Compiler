package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser

class ArrayCreationExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.ArrayCreationExprContext) {
        codeGenerationPhase.visit(ctx.size)
        if (ctx.size.staticType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, ctx.size.staticType, ctx.size.start)
        }
        val arrayType = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(codeGenerationPhase.classSymbolTable)
            ?: TODO()

        val sizeVariable = if (codeGenerationPhase.localsVariableSymbolTable.isDeclared("#size")) {
            codeGenerationPhase.localsVariableSymbolTable.addressOf("#size")
        } else {
            codeGenerationPhase.localsVariableSymbolTable.declareVariable("#size", DataType.PrimitiveType.Integer)
        }

        // store array size in #size (no dup)
        // https://github.com/WebAssembly/design/issues/1102
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_tee(sizeVariable))

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(arrayType.sizeInBytes()))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_mul)

        // 4 extra bytes for array size
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(4))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        // allocate memory
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(codeGenerationPhase.mallocAddress))

        val arrayAddressVariable = if (codeGenerationPhase.localsVariableSymbolTable.isDeclared("#array")) {
            codeGenerationPhase.localsVariableSymbolTable.addressOf("#array")
        } else {
            codeGenerationPhase.localsVariableSymbolTable.declareVariable("#array", DataType.PrimitiveType.Integer)
        }

        // store array address in #array
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_tee(arrayAddressVariable))

        // store array size in first 4 bytes
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_get(sizeVariable))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_store)

        // put array address on top of stack
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_get(arrayAddressVariable))

        ctx.staticType = DataType.Array(arrayType)
    }
}