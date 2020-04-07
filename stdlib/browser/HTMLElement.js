module.exports = runtime => {
    return {
        "HTMLElement.appendChild#HTMLElement": (thisRef, newChildRef) => {
            runtime.wasmDeref(thisRef).appendChild(runtime.wasmDeref(newChildRef));
        },
    }
};