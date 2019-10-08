package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function

class ModuleWriter {

    companion object {
        fun toSExpr(module: Module): String {
            val sb = StringBuilder()
            writeModule(module, sb)
            return sb.toString()
        }

        private fun writeModule(module: Module, sb: StringBuilder) {
            sb.append("(module ")
            module.types.forEach {
                writeType(it, sb)
            }
            module.imports.forEach {
                writeImport(it, sb)
            }
            module.funcs.forEach {
                writeFunction(it, sb)
            }
            module.exports.forEach {
                writeExport(it, sb)
            }
            sb.append(")")
        }

        private fun writeType(type: FuncType, sb: StringBuilder) {
            sb.append("(type (func ")
            type.parameters.forEach {
                sb.append("(param ${asText(it)}) ")
            }
            type.result.forEach {
                sb.append("(result ${asText(it)}) ")
            }
            sb.append(")) ")
        }

        private fun asText(type: ValueType): String {
            return when (type) {
                ValueType.I32 -> "i32"
                ValueType.I64 -> "i64"
                ValueType.F32 -> "f32"
                ValueType.F64 -> "f64"
            }
        }

        private fun writeImport(import: Import, sb: StringBuilder) {
            sb.append("(import \"${import.module}\" \"${import.name}\" ")
            when (import.desc) {
                is ImportDesc.Func -> sb.append("(func (type ${import.desc.typeidx}))")
            }
            sb.append(") ")
        }

        private fun writeFunction(function: Function, sb: StringBuilder) {
            sb.append("(func (type ${function.typeidx}) ")
            function.locals.forEach {
                sb.append("(local ${asText(it)}) ")
            }
            function.body.instructions.forEach {
                sb.append("${asText(it)} ")
            }
            sb.append(") ")
        }

        private fun asText(instruction: Instruction): String {
            return when (instruction) {
                is Instruction.i32_const -> "i32.const ${instruction.value}"
                is Instruction.call -> "call ${instruction.address}"
                is Instruction.local_set -> "local.set ${instruction.address}"
                is Instruction.local_get -> "local.get ${instruction.address}"
            }
        }

        private fun writeExport(export: Export, sb: StringBuilder) {
            sb.append("(export \"${export.name}\" ")
            when (export.desc) {
                is ExportDesc.Func -> sb.append("(func ${export.desc.funcidx})")
            }
            sb.append(") ")
        }

    }
}