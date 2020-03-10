package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

abstract class CodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    var ParseTree.staticType: DataType?
        get() = codeGenerationPhase.staticTypes.get(this)
        set(type) = codeGenerationPhase.staticTypes.put(this, type)
}