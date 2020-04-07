module.exports = runtime => {
    return {
        'Document.getElementById#String': elementIdRef => {
            const elementId = runtime.wasmDeref(elementIdRef);
            return runtime.wasmRef(document.getElementById(elementId), 'HTMLElement');
        },
        'Document.createElement#String': tagNameRef => {
            const tagName = runtime.wasmDeref(tagNameRef);
            return runtime.wasmRef(document.createElement(tagName), 'HTMLElement');
        }
    };
};