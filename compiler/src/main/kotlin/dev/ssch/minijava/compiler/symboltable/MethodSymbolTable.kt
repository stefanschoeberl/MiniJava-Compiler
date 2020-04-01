package dev.ssch.minijava.compiler.symboltable

import dev.ssch.minijava.compiler.DataType

class MethodSymbolTable {

    data class MethodSignature(
        val name: String,
        val parameterTypes: List<DataType>
    )

    class MethodInformation(
        var address: Int,
        val returnType: DataType?,
        val isPublic: Boolean,
        val isStatic: Boolean
    )

    val nativeMethods = mutableMapOf<MethodSignature, MethodInformation>()
    val methods = mutableMapOf<MethodSignature, MethodInformation>()

    fun isDeclared(name: String, parameters: List<DataType>): Boolean {
        val signature =
            MethodSignature(name, parameters)
        return nativeMethods.contains(signature) || methods.contains(signature)
    }

    fun isNative(name: String, parameters: List<DataType>): Boolean {
        return nativeMethods.contains(MethodSignature(name, parameters))
    }

    fun isStatic(name: String, parameters: List<DataType>): Boolean {
        return findMethodInformation(name, parameters)!!.isStatic
    }

    fun declareNativeMethod(address: Int, returnType: DataType?, name: String, parameters: List<DataType>, isPublic: Boolean, isStatic: Boolean): MethodInformation {
        val signature =
            MethodSignature(name, parameters)
        val methodInformation = MethodInformation(
            address,
            returnType,
            isPublic,
            isStatic
        )
        nativeMethods[signature] = methodInformation
        return methodInformation
    }

    fun declareMethod(address: Int, returnType: DataType?, name: String, parameters: List<DataType>, isPublic: Boolean, isStatic: Boolean): MethodInformation {
        val signature =
            MethodSignature(name, parameters)
        val methodInformation = MethodInformation(
            address,
            returnType,
            isPublic,
            isStatic
        )
        methods[signature] = methodInformation
        return methodInformation
    }

    private fun findMethodInformation(name: String, parameters: List<DataType>): MethodInformation? {
        val signature =
            MethodSignature(name, parameters)
        val nativeMethod = nativeMethods[signature]
        if (nativeMethod != null) {
            return nativeMethod
        }

        return methods[signature]
    }
    fun addressOf(name: String, parameters: List<DataType>): Int = findMethodInformation(name, parameters)!!.address
    fun returnTypeOf(name: String, parameters: List<DataType>): DataType? = findMethodInformation(name, parameters)!!.returnType

}