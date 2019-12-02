package dev.ssch.minijava

class MethodSymbolTable {

    data class MethodSignature(
        val name: String,
        val parameterTypes: List<DataType>
    )

    class MethodInformation(
        val address: Int,
        val returnType: DataType?,
        val isPublic: Boolean
    )

    val nativeMethods = mutableMapOf<MethodSignature, MethodInformation>()
    val methods = mutableMapOf<MethodSignature, MethodInformation>()

    fun isDeclared(name: String, parameters: List<DataType>): Boolean {
        val signature = MethodSignature(name, parameters)
        return nativeMethods.contains(signature) || methods.contains(signature)
    }

    fun declareNativeMethod(returnType: DataType?, name: String, parameters: List<DataType>, isPublic: Boolean) {
        val signature = MethodSignature(name, parameters)
        nativeMethods[signature] = MethodInformation(nativeMethods.size, returnType, isPublic)
    }

    fun declareMethod(returnType: DataType?, name: String, parameters: List<DataType>, isPublic: Boolean) {
        val signature = MethodSignature(name, parameters)
        methods[signature] = MethodInformation(methods.size, returnType, isPublic)
    }

    private fun findMethodInformation(name: String, parameters: List<DataType>): MethodInformation? {
        val signature = MethodSignature(name, parameters)
        val nativeMethod = nativeMethods[signature]
        if (nativeMethod != null) {
            return nativeMethod
        }

        return methods[signature]?.let { MethodInformation(nativeMethods.size + it.address, it.returnType, it.isPublic) }
    }

    fun addressOf(name: String, parameters: List<DataType>): Int = findMethodInformation(name, parameters)!!.address
    fun returnTypeOf(name: String, parameters: List<DataType>): DataType? = findMethodInformation(name, parameters)!!.returnType

}