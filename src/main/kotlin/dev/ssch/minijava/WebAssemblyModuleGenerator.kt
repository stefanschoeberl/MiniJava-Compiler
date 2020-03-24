package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function

class WebAssemblyModuleGenerator {

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
            is ImportDesc.Memory -> sb.append("(memory ${import.desc.memtype.min})")
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
            is Instruction.f32_const -> "f32.const ${instruction.value}"
            is Instruction.call -> "call ${instruction.address}"
            is Instruction.local_set -> "local.set ${instruction.address}"
            is Instruction.local_get -> "local.get ${instruction.address}"
            is Instruction.local_tee -> "local.tee ${instruction.address}"
            Instruction.i32_add -> "i32.add"
            Instruction.i32_sub -> "i32.sub"
            Instruction.i32_mul -> "i32.mul"
            Instruction.i32_div_s -> "i32.div_s"
            Instruction.i32_eq -> "i32.eq"
            Instruction.i32_ne -> "i32.ne"
            Instruction.f32_eq -> "f32.eq"
            Instruction.f32_ne -> "f32.ne"
            Instruction.f32_add -> "f32.add"
            Instruction.f32_sub -> "f32.sub"
            Instruction.f32_mul -> "f32.mul"
            Instruction.f32_div -> "f32.div"
            Instruction._if -> "if"
            Instruction._else -> "else"
            Instruction.end -> "end"
            Instruction.i32_and -> "i32.and"
            Instruction.i32_or -> "i32.or"
            is Instruction.br -> "br ${instruction.label}"
            is Instruction.br_if -> "br_if ${instruction.label}"
            Instruction.i32_eqz -> "i32.eqz"
            Instruction.block -> "block"
            Instruction.loop -> "loop"
            Instruction.i32_lt_s -> "i32.lt_s"
            Instruction.i32_le_s -> "i32.le_s"
            Instruction.i32_gt_s -> "i32.gt_s"
            Instruction.i32_ge_s -> "i32.ge_s"
            Instruction.f32_lt -> "f32.lt"
            Instruction.f32_le -> "f32.le"
            Instruction.f32_gt -> "f32.gt"
            Instruction.f32_ge -> "f32.ge"
            Instruction._return -> "return"
            Instruction.drop -> "drop"
            Instruction.unreachable -> "unreachable"
            Instruction.f32_convert_i32_s -> "f32.convert_i32_s"
            Instruction.i32_trunc_f32_s -> "i32.trunc_f32_s"
            Instruction.i32_store -> "i32.store"
            Instruction.f32_store -> "f32.store"
            Instruction.i32_store8 -> "i32.store8"
            Instruction.i32_load -> "i32.load"
            Instruction.f32_load -> "f32.load"
            Instruction.i32_load8_s -> "i32.load8_s"
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