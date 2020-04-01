package dev.ssch.minijava.symboltable

import dev.ssch.minijava.DataType
import dev.ssch.minijava.removeFirstOrNull

class LocalVariableSymbolTable {

    class SymbolInformation (
        val address: Int,
        val type: DataType
    )

    private val scopes = mutableListOf(mutableSetOf<String>())
    private val symbols = mutableMapOf<String, SymbolInformation>()

    private var parameterCount = 0
    private val localVariables = mutableListOf<DataType>()
    private val unusedVariables = mutableMapOf<DataType, MutableList<Int>>()

    private var thisAddress: Int? = null

    private fun allocateNewAddressForParameter(): Int {
        return parameterCount++
    }

    private fun allocateNewAddressForLocal(type: DataType): Int {
        val address = localVariables.size + parameterCount
        localVariables.add(type)
        return address
    }

    private fun getFreeAddressForLocal(type: DataType): Int {
        val addressPool = unusedVariables[type] ?: return allocateNewAddressForLocal(type)
        return addressPool.removeFirstOrNull() ?: return allocateNewAddressForLocal(type)
    }

    fun declareVariable(name: String, type: DataType): Int {
        val addr = getFreeAddressForLocal(type)
        symbols[name] = SymbolInformation(addr, type)
        scopes.first().add(name)
        return addr
    }

    fun declareParameter(name: String, type: DataType): Int {
        val addr = allocateNewAddressForParameter()
        symbols[name] = SymbolInformation(addr, type)
        scopes.first().add(name)
        return addr
    }

    fun declareThisParameter(): Int {
        return allocateNewAddressForParameter().also {
            thisAddress = it
        }
    }

    fun doesThisParameterExist(): Boolean {
        return thisAddress != null
    }

    fun isDeclared(name: String): Boolean = symbols.containsKey(name)

    fun addressOf(name: String): Int = symbols[name]!!.address
    fun typeOf(name: String): DataType = symbols[name]!!.type

    fun addressOfThis(): Int {
        return thisAddress!!
    }

    fun pushScope() {
        scopes.add(0, mutableSetOf())
    }

    fun popScope() {
        val scope = scopes.removeAt(0)
        scope.forEach {
            val symbolInformation = symbols.remove(it)!!
            unusedVariables.getOrPut(symbolInformation.type) { mutableListOf() }.add(symbolInformation.address)
        }
    }

    val allLocalVariables: List<DataType>
        get() = localVariables.toList()
}