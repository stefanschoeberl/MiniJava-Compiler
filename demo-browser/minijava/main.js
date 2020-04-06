module.exports = function(runtime) {
    return {
        "Main.alert#String": (stringRef) => {
            alert(runtime.wasmDeref(stringRef));
        }
    }
};