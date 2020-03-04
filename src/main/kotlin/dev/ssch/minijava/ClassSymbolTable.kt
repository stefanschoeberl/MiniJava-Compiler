package dev.ssch.minijava

class ClassSymbolTable {

    class ClassInformation(
        val methodSymbolTable: MethodSymbolTable
    )

    val classes = mutableMapOf<String, ClassInformation>()

    // TODO isDeclared

    fun declareClass(className: String): ClassInformation {
        val classInformation = ClassInformation(MethodSymbolTable())
        classes[className] = classInformation
        return classInformation
    }

    fun getMethodSymbolTable(className: String): MethodSymbolTable {
        return classes[className]!!.methodSymbolTable
    }
}