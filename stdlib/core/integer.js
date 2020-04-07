module.exports = runtime => {
    return {
        "Integer.parseInt#String": stringRef => {
            return parseInt(runtime.wasmDeref(stringRef));
        },
        "Integer.toString#int": i => {
            return runtime.wasmRef(i.toString());
        }
    };
};