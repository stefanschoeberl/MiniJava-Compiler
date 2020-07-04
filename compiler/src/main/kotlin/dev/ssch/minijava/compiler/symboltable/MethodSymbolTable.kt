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

    fun isCallable(name: String, parameters: List<DataType>): Boolean {
        return findMethodInformation(name, parameters) != null
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

    private fun findMethodInformationWithoutObjectReplacement(name: String, parameters: List<DataType>): MethodInformation? {
        val signature =
            MethodSignature(name, parameters)
        val nativeMethod = nativeMethods[signature]
        if (nativeMethod != null) {
            return nativeMethod
        }
        return methods[signature]
    }

    private fun findMethodInformation(name: String, parameters: List<DataType>): MethodInformation? {
        val methodInfo = findMethodInformationWithoutObjectReplacement(name, parameters)
        if (methodInfo != null) {
            return methodInfo
        }

        // try all combinations of ReferenceType/Object
        val referenceTypeIndices = parameters
            .map { it != DataType.ReferenceType.ObjectType && (it is DataType.ReferenceType || it is DataType.Array) }
            .mapIndexed { index, b -> Pair(index, b) }
            .filter { it.second }
            .map { it.first }

        if (referenceTypeIndices.isEmpty()) {
            return null
        }

        val parameterCombination = parameters.toMutableList()
        fun isLastParameterCombination(): Boolean {
            return referenceTypeIndices
                .map { parameterCombination[it] == DataType.ReferenceType.ObjectType }
                .all { it }
        }
        fun nextCombination(index: Int = 0) {
            if (index < referenceTypeIndices.size) {
                val indexInParameters = referenceTypeIndices[index]
                if (parameterCombination[indexInParameters] != DataType.ReferenceType.ObjectType) {
                    parameterCombination[indexInParameters] = DataType.ReferenceType.ObjectType
                } else {
                    parameterCombination[indexInParameters] = parameters[indexInParameters]
                    nextCombination(index + 1)
                }
            }
        }

        do {
            nextCombination()
            val currentMethodInfo = findMethodInformationWithoutObjectReplacement(name, parameterCombination)
            if (currentMethodInfo != null) {
                return currentMethodInfo
            }
        } while (!isLastParameterCombination())
        return null
    }

    fun addressOf(name: String, parameters: List<DataType>): Int = findMethodInformation(name, parameters)!!.address
    fun returnTypeOf(name: String, parameters: List<DataType>): DataType? = findMethodInformation(name, parameters)!!.returnType

}