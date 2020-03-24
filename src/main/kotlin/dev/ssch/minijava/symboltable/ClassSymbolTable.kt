package dev.ssch.minijava.symboltable

class ClassSymbolTable {

    class ClassInformation(
        val methodSymbolTable: MethodSymbolTable,
        val fieldSymbolTable: FieldSymbolTable,
        val initializerSymbolTable: InitializerSymbolTable,
        var constructorAddress: Int
    )

    val classes = mutableMapOf<String, ClassInformation>()

    fun isDeclared(className: String): Boolean {
        return classes.containsKey(className)
    }

    fun declareClass(constructorAddress: Int, className: String): ClassInformation {
        val classInformation = ClassInformation(
            MethodSymbolTable(),
            FieldSymbolTable(),
            InitializerSymbolTable(),
            constructorAddress
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

    fun getInitializerSymbolTable(className: String): InitializerSymbolTable {
        return classes[className]!!.initializerSymbolTable
    }

    fun getConstructorAddress(className: String): Int {
        return classes[className]!!.constructorAddress
    }
}