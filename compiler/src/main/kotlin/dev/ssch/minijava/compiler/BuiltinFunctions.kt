package dev.ssch.minijava.compiler

class BuiltinFunctions {
    companion object {
        const val NUM_FUNCTIONS = 26
    }

    var newArrayNumericAddress: Int = -1
    var newArrayBooleanAddress: Int = -1
    var newArrayCharAddress: Int = -1
    var newArrayReferenceAddress: Int = -1

    var arrayLengthAddress: Int = -1

    var getArrayPrimitiveIntAddress: Int = -1
    var getArrayPrimitiveFloatAddress: Int = -1
    var getArrayPrimitiveBooleanAddress: Int = -1
    var getArrayPrimitiveCharAddress: Int = -1
    var getArrayReferenceAddress: Int = -1

    var setArrayPrimitiveIntAddress: Int = -1
    var setArrayPrimitiveFloatAddress: Int = -1
    var setArrayPrimitiveBooleanAddress: Int = -1
    var setArrayPrimitiveCharAddress: Int = -1
    var setArrayReferenceAddress: Int = -1

    var concatStringStringAddress: Int = -1
    var concatStringIntAddress: Int = -1
    var concatIntStringAddress: Int = -1
    var concatStringFloatAddress: Int = -1
    var concatFloatStringAddress: Int = -1
    var concatStringBooleanAddress: Int = -1
    var concatBooleanStringAddress: Int = -1
    var concatStringCharAddress: Int = -1
    var concatCharStringAddress: Int = -1
    var concatStringReferenceAddress: Int = -1
    var concatReferenceStringAddress: Int = -1
}