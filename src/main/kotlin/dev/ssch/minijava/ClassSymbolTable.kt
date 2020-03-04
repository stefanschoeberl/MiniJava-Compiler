package dev.ssch.minijava

class ClassSymbolTable {

    class ClassInformation(
        val methodSymbolTable: MethodSymbolTable,
        val fieldSymbolTable: MutableMap<String, DataType>
    )

    val classes = mutableMapOf<String, ClassInformation>()

    fun isDeclared(className: String): Boolean {
        return classes.containsKey(className)
    }

    fun declareClass(className: String): ClassInformation {
        val classInformation = ClassInformation(MethodSymbolTable(), mutableMapOf())
        classes[className] = classInformation
        return classInformation
    }

    fun getMethodSymbolTable(className: String): MethodSymbolTable {
        return classes[className]!!.methodSymbolTable
    }

    fun getFieldsOfClass(className: String): Map<String, DataType> {
        return classes[className]!!.fieldSymbolTable
    }
}