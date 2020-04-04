module.exports = function (runtime) {
    return {
        "String.length": function (thisRef) {
            return runtime.wasmDeref(thisRef).length;
        },
        "String.charAt#int": function (thisRef, index) {
            return runtime.charToWasm(runtime.wasmDeref(thisRef)[index]);
        },
        "String.equals#String": function (thisRef, other) {
            return runtime.wasmDeref(thisRef) === runtime.wasmDeref(other);
        }
    };
};