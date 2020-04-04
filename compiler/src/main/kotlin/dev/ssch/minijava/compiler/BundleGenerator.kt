package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.compiler.symboltable.StringLiteralSymbolTable
import dev.ssch.minijava.wasm.WebAssemblyAssembler
import dev.ssch.minijava.wasm.WebAssemblyModuleGenerator
import java.io.File

class BundleGenerator (
    private val moduleGenerator: WebAssemblyModuleGenerator,
    private val assembler: WebAssemblyAssembler,
    private val externalFunctionNameProvider: ExternalFunctionNameProvider
) {

    fun generateBundle(bundle: Bundle, src: List<File>, outputFolder: File) {
        outputFolder.mkdirs()
        val wat = File(outputFolder, "module.wat")
        val watText = moduleGenerator.toSExpr(bundle.module)
        wat.writeText(watText)

        val wasm = File(outputFolder, "module.wasm")
        assembler.assemble(wat, wasm)

        val nativeFolder = File(outputFolder, "native")
        nativeFolder.mkdir()

        val scriptFiles = copyScriptFiles(src, nativeFolder)
        generateModuleJS(outputFolder, nativeFolder, scriptFiles, bundle.classSymbolTable, bundle.stringLiteralSymbolTable)
    }

    private fun generateModuleJS(
        outputFolder: File,
        nativeFolder: File,
        scriptFiles: List<File>,
        classSymbolTable: ClassSymbolTable,
        stringLiteralSymbolTable: StringLiteralSymbolTable
    ) {
        val exports = scriptFiles.map { "require('./${nativeFolder.name}/${it.name}')" }.toMutableList()
        exports.add(generateObjectHelper(classSymbolTable))
        exports.add(0, generateStringLiterals(stringLiteralSymbolTable))
        val content = "module.exports = [${exports.joinToString()}];"
        val moduleJS = File(outputFolder, "module.js")
        moduleJS.writeText(content)
    }

    private fun generateObjectHelper(classSymbolTable: ClassSymbolTable): String {
        val functions = mutableListOf<String>()

        classSymbolTable.classes.forEach {
            val className = it.key
            val fields = it.value.fieldSymbolTable.fields.entries
            val initObject = fields.joinToString(",") { field ->
                val initValue = when (field.value.type) {
                    DataType.PrimitiveType.Integer -> "0"
                    DataType.PrimitiveType.Boolean -> "false"
                    DataType.PrimitiveType.Float -> "0"
                    DataType.PrimitiveType.Char -> "'\\0'"
                    is DataType.ReferenceType -> "null"
                    is DataType.Array -> "null"
                    DataType.NullType -> TODO()
                }
                "\"${field.key}\":$initValue"
            }

            val constructor = externalFunctionNameProvider.externalNameForConstructor(className)
            functions.add("\"$constructor\": function() { return runtime.wasmRef({$initObject}); }")
            fields.forEach { field ->
                val getter = externalFunctionNameProvider.externalNameForGetter(className, field.key)
                val setter = externalFunctionNameProvider.externalNameForSetter(className, field.key)
                val type = field.value.type

                if (type is DataType.PrimitiveType.Boolean) {
                    functions.add("\"$getter\": function (ref) { return runtime.wasmDeref(ref)[\"${field.key}\"]; }")
                    functions.add("\"$setter\": function (ref, val) { runtime.wasmDeref(ref)[\"${field.key}\"] = runtime.wasmBoolean(val); }")
                } else if (type is DataType.ReferenceType || type is DataType.Array) {
                    functions.add("\"$getter\": function (ref) { return runtime.wasmRef(runtime.wasmDeref(ref)[\"${field.key}\"]); }")
                    functions.add("\"$setter\": function (ref, val) { runtime.wasmDeref(ref)[\"${field.key}\"] = runtime.wasmDeref(val); }")
                } else {
                    functions.add("\"$getter\": function (ref) { return runtime.wasmDeref(ref)[\"${field.key}\"]; }")
                    functions.add("\"$setter\": function (ref, val) { runtime.wasmDeref(ref)[\"${field.key}\"] = val; }")
                }
            }
        }

        val classCode = functions.joinToString(",\n") {
            it.prependIndent("                    ")
        }
        return """
            function (runtime) {
                return {
$classCode
                };
            }
        """.trimIndent()
    }

    private fun generateStringLiterals(stringLiteralSymbolTable: StringLiteralSymbolTable): String {
        val stringsCode = stringLiteralSymbolTable.allStringsByAddress
            .sortedBy { it.first }
            .map { """runtime.wasmRef("${it.second}");""" }
            .joinToString("\n") { it.prependIndent("                ")}

        return """
            function (runtime) {
$stringsCode
                return {};
            }
        """.trimIndent()
    }

    private fun copyScriptFiles(src: List<File>, nativeFolder: File): List<File> {
        val scriptFiles = src.map {file ->
            val name = file.nameWithoutExtension
            File(file.parent, "$name.js")
        }.filter { it.isFile }

        val copyFromTo = scriptFiles.mapIndexed { index, scriptFile ->
            Pair(scriptFile, File(nativeFolder, "$index.js"))
        }
        copyFromTo.forEach {
            it.first.copyTo(it.second, true)
        }

        return copyFromTo.map { it.second }
    }
}