module.exports = runtime => {
    return {
        "HTMLElement.appendChild#HTMLElement": (thisRef, newChildRef) => {
            runtime.wasmDeref(thisRef).appendChild(runtime.wasmDeref(newChildRef));
        },
        "HTMLElement.addClickEventListener#Object": (thisRef, handlerRef) => {
            const element = runtime.wasmDeref(thisRef);
            const handler = runtime.wasmDeref(handlerRef);
            element.addEventListener('click', (event) => {
                runtime.instanceMethod(handler, 'handleEvent', 'MouseEvent')(event);
            })
        }
    }
};