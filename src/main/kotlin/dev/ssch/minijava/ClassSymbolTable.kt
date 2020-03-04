package dev.ssch.minijava

class ClassSymbolTable {

    class ClassInformation(
        val methodSymbolTable: MethodSymbolTable
    )

    val classes = mutableMapOf<String, ClassInformation>()

    fun isDeclared(className: String): Boolean {
        return classes.containsKey(className)
    }

    fun declareClass(className: String): ClassInformation {
        val classInformation = ClassInformation(MethodSymbolTable())
        classes[className] = classInformation
        return classInformation
    }

    fun getMethodSymbolTable(className: String): MethodSymbolTable {
        return classes[className]!!.methodSymbolTable
    }
}