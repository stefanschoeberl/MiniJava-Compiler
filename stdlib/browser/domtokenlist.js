module.exports = runtime => {
    return {
        "DOMTokenList.add#String": (thisRef, tokenRef) => {
            runtime.wasmDeref(thisRef).add(runtime.wasmDeref(tokenRef));
        },
        "DOMTokenList.remove#String": (thisRef, tokenRef) => {
            runtime.wasmDeref(thisRef).remove(runtime.wasmDeref(tokenRef));
        },
    }
};