package dev.ssch.minijava.ast

sealed class ExportDesc {
    data class Func(
        val funcidx: Int
    ) : ExportDesc()
}