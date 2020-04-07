module.exports = runtime => {
    function writeArrayMap(f) {
        return ref => {
            const array = runtime.wasmDeref(ref);
            if (array != null) {
                console.log('[' + array.map(f).join(', ') + ']');
            } else {
                console.log(null);
            }
        }
    }

    const writeArray = writeArrayMap(x => x);

    return {
        'Console.println#int': arg => {
            console.log(arg);
        },
        'Console.println#float': arg => {
            console.log(arg);
        },
        'Console.println#boolean': arg => {
            console.log(runtime.wasmBoolean(arg));
        },
        'Console.println#char': arg => {
            console.log(runtime.wasmToChar(arg));
        },
        'Console.println#String': arg => {
            console.log(runtime.wasmDeref(arg));
        },
        'Console.println#Object': arg => {
            console.log(runtime.wasmDeref(arg));
        },
        'Console.println#int[]': writeArray,
        'Console.println#boolean[]': writeArray,
        'Console.println#float[]': writeArray,
        'Console.println#char[]': writeArrayMap(function (c) { return "'" + c + "'" }),
    };
};