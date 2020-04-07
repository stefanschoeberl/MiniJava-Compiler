class Runtime {

    constructor() {
        this.nextFreeReference = 1;
        this.objects = new Map();
        this.references = new Map();
        this.objects.set(null, 0);
        this.references.set(0, null);
        this.instance = null;
    }

    setWasmModuleInstance(instance) {
        this.instance = instance
    }

    wasmRef(obj) {
        if (this.objects.has(obj)) {
            return this.objects.get(obj);
        } else {
            const address = this.nextFreeReference++;
            this.objects.set(obj, address);
            this.references.set(address, obj);
            return address;
        }
    }

    wasmDeref(address) {
        return this.references.get(address);
    }

    wasmBoolean(value) {
        return value !== 0
    }

    wasmToChar(value) {
        return String.fromCodePoint(value);
    }

    charToWasm(value) {
        return value.codePointAt(0);
    }

    staticMethod(className, methodName, ...argumentTypes) {
        const argumentTypeString = argumentTypes.map(s => "#" + s).join("");
        return this.instance.exports[className + "." + methodName + argumentTypeString];
    }
}

module.exports = Runtime;