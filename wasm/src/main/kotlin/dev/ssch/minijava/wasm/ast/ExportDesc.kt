package dev.ssch.minijava.wasm.ast

sealed class ExportDesc {
    data class Func(
        val funcidx: Int
    ) : ExportDesc()
}