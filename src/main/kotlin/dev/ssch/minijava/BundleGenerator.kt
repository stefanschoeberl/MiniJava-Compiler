package dev.ssch.minijava

import dev.ssch.minijava.ast.Module
import dev.ssch.minijava.symboltable.ClassSymbolTable
import java.io.File

class BundleGenerator {

    private val moduleGenerator = WebAssemblyModuleGenerator()
    private val assembler = WebAssemblyAssembler()

    fun generateBundle(module: Module, src: List<File>, outputFolder: File, classSymbolTable: ClassSymbolTable) {
        outputFolder.mkdirs()
        val wat = File(outputFolder, "module.wat")
        val watText = moduleGenerator.toSExpr(module)
        wat.writeText(watText)

        val wasm = File(outputFolder, "module.wasm")
        assembler.assemble(wat, wasm)

        val nativeFolder = File(outputFolder, "native")
        nativeFolder.mkdir()

        val scriptFiles = copyScriptFiles(src, nativeFolder)
        generateModuleJS(outputFolder, nativeFolder, scriptFiles, classSymbolTable)
    }

    private fun generateModuleJS(outputFolder: File, nativeFolder: File, scriptFiles: List<File>, classSymbolTable: ClassSymbolTable) {
        val exports = scriptFiles.map { "require('./${nativeFolder.name}/${it.name}')" }.toMutableList()
        exports.add(generateObjectHelper(classSymbolTable))
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
                    is DataType.ReferenceType -> "null"
                    is DataType.Array -> "null"
                    DataType.NullType -> TODO()
                }
                "\"${field.key}\":$initValue"
            }

            val constructor = externalConstructorName(className)
            functions.add("\"$constructor\": function() { return runtime.wasmRef({$initObject}); }")
            fields.forEach { field ->
                val getter = externalGetterName(className, field.key)
                val setter = externalSetterName(className, field.key)
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

        val classCode = functions
            .map { it.prependIndent("                    ") }
            .joinToString(",\n")
        return """
            function (runtime) {
                return {
$classCode
                }
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