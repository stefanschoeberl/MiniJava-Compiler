package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.symboltable.MethodSymbolTable

class ExternalFunctionNameProvider {

    fun externalNameForMethod(signature: MethodSymbolTable.MethodSignature): String {
        return signature.name + signature.parameterTypes.joinToString("") { "#$it" }
    }

    fun externalNameForConstructor(className: String): String {
        return "new_$className"
    }

    fun externalNameForGetter(className: String, fieldName: String): String {
        return "get_$className.$fieldName"
    }

    fun externalNameForSetter(className: String, fieldName: String): String {
        return "set_$className.$fieldName"
    }
}