package dev.ssch.minijava.symboltable

import dev.ssch.minijava.DataType

class ConstructorSymbolTable {

    data class ConstructorSignature(
        val parameterTypes: List<DataType>
    )

    class ConstructorInformation(
        var address: Int
    )

    val constructors = mutableMapOf<ConstructorSignature, ConstructorInformation>()

    fun declareConstructor(address: Int, parameters: List<DataType>): ConstructorInformation {
        val signature = ConstructorSignature(parameters)
        val constructorInformation = ConstructorInformation(address)
        constructors[signature] = constructorInformation
        return constructorInformation
    }

    fun isDeclared(parameters: List<DataType>): Boolean {
        return constructors.contains(ConstructorSignature(parameters))
    }

    private fun findConstructorInformation(parameters: List<DataType>): ConstructorInformation? {
        return constructors[ConstructorSignature(parameters)]
    }

    fun addressOf(parameters: List<DataType>): Int = findConstructorInformation(parameters)!!.address
}