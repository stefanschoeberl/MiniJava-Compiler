module.exports = function (runtime) {
    function writeArrayMap(f) {
        return function(ref) {
            const array = runtime.wasmDeref(ref);
            if (array != null) {
                console.log('[' + array.map(f).join(', ') + ']');
            } else {
                console.log(null);
            }
        }
    }

    var writeArray = writeArrayMap(function (x) {return x;});

    return {
        "Console.println#int": function(arg) {
            console.log(arg)
        },
        "Console.println#float": function(arg) {
            console.log(arg)
        },
        "Console.println#boolean": function(arg) {
            console.log(runtime.wasmBoolean(arg))
        },
        "Console.println#char": function(arg) {
            console.log(runtime.wasmToChar(arg))
        },
        "Console.println#int[]": writeArray,
        "Console.println#boolean[]": writeArray,
        "Console.println#float[]": writeArray,
        "Console.println#char[]": writeArrayMap(function (c) { return "'" + c + "'" }),
    }
};