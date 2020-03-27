module.exports = function (runtime) {
    function writeArray(ref) {
        const array = runtime.wasmDeref(ref);
        if (array != null) {
            console.log('[' + array.join(', ') + ']');
        } else {
            console.log(null);
        }
    }

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
        "Console.println#int[]": writeArray,
        "Console.println#boolean[]": writeArray,
        "Console.println#float[]": writeArray,
    }
};