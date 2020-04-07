module.exports = runtime => {
    return {
        'Range.printToConsole': thisRef => {
            console.log(runtime.wasmDeref(thisRef));
        }
    };
};