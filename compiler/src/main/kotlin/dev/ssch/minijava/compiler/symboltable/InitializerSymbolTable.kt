package dev.ssch.minijava.compiler.symboltable

import dev.ssch.minijava.compiler.DataType

class InitializerSymbolTable {

    data class InitializerSignature(
        val parameterTypes: List<DataType>
    )

    class InitializerInformation(
        var address: Int
    )

    val initializers = mutableMapOf<InitializerSignature, InitializerInformation>()

    fun declareInitializer(address: Int, parameters: List<DataType>): InitializerInformation {
        val signature = InitializerSignature(parameters)
        val initializerInformation = InitializerInformation(address)
        initializers[signature] = initializerInformation
        return initializerInformation
    }

    fun isDeclared(parameters: List<DataType>): Boolean {
        return initializers.contains(InitializerSignature(parameters))
    }

    private fun findInitializerInformation(parameters: List<DataType>): InitializerInformation? {
        return initializers[InitializerSignature(parameters)]
    }

    fun addressOf(parameters: List<DataType>): Int = findInitializerInformation(parameters)!!.address
}