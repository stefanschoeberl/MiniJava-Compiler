package dev.ssch.minijava.ast

sealed class ImportDesc {
    data class Func(
        val typeidx: Int
    ) : ImportDesc()
}