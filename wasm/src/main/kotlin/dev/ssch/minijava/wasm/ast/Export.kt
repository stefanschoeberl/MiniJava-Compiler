package dev.ssch.minijava.wasm.ast

data class Export (
    val name: String,
    val desc: ExportDesc
)