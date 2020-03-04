package dev.ssch.minijava.symboltable

class ClassSymbolTable {

    class ClassInformation(
        val methodSymbolTable: MethodSymbolTable,
        val fieldSymbolTable: FieldSymbolTable
    )

    val classes = mutableMapOf<String, ClassInformation>()

    fun isDeclared(className: String): Boolean {
        return classes.containsKey(className)
    }

    fun declareClass(className: String): ClassInformation {
        val classInformation = ClassInformation(
            MethodSymbolTable(),
            FieldSymbolTable()
        )
        classes[className] = classInformation
        return classInformation
    }

    fun getMethodSymbolTable(className: String): MethodSymbolTable {
        return classes[className]!!.methodSymbolTable
    }

    fun getFieldSymbolTable(className: String): FieldSymbolTable {
        return classes[className]!!.fieldSymbolTable
    }
}