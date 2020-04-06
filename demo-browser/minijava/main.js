module.exports = runtime => {
    return {
        "Main.alert#String": stringRef => {
            alert(runtime.wasmDeref(stringRef));
        }
    }
};