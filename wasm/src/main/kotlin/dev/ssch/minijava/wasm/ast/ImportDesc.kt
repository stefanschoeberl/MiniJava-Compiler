package dev.ssch.minijava.wasm.ast

sealed class ImportDesc {
    data class Func(
        val typeidx: Int
    ) : ImportDesc()

    data class Memory(
        val memtype: MemType
    ): ImportDesc()
}