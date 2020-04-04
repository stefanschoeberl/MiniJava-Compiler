const fs = require('fs');
const path = require('path');
const { promisify } = require("util");

// https://webassembly.org/getting-started/js-api/

const readFile = promisify(fs.readFile);

async function runModule(folder) {
    const folderAbsolute = path.resolve(folder);
    const file = path.join(folderAbsolute, "module.wasm");
    const scripts = require(path.join(folderAbsolute, "module"));


    let nextFreeReference = 1;
    const objects = new Map();
    const references = new Map();

    objects.set(null, 0);
    references.set(0, null);

    const runtime = {
        wasmRef: (obj) => {
            if (objects.has(obj)) {
                return objects.get(obj);
            } else {
                const address = nextFreeReference++;
                objects.set(obj, address);
                references.set(address, obj);
                return address;
            }
        },
        wasmDeref: (address) => {
            return references.get(address);
        },
        wasmBoolean: (value) => {
            return value !== 0
        },
        wasmToChar: (value) => {
            return String.fromCodePoint(value);
        },
        charToWasm: (value) => {
            return value.codePointAt(0);
        }
    };

    const nativeMethods = {};
    for (let script of scripts) {
        const imports = script(runtime);
        for (let name in imports) {
            if (imports.hasOwnProperty(name)) {
                nativeMethods[name] = imports[name];
            }
        }
    }

    const imports = {
        internal: {
            "new_array_numeric": size => {
                return runtime.wasmRef(Array(size).fill(0))
            },
            "new_array_boolean": size => {
                return runtime.wasmRef(Array(size).fill(false))
            },
            "new_array_char": size => {
                return runtime.wasmRef(Array(size).fill('\0'))
            },
            "new_array_reference": size => {
                return runtime.wasmRef(Array(size).fill(null))
            },
            "get_array_numeric": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return a[index];
            },
            "get_array_boolean": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return a[index] ? 1 : 0;
            },
            "get_array_char": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return runtime.charToWasm(a[index]);
            },
            "get_array_reference": (arrayAddress, index) => {
                const a = runtime.wasmDeref(arrayAddress);
                return runtime.wasmRef(a[index]);
            },
            "set_array_numeric": (arrayAddress, index, value) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = value;
            },
            "set_array_boolean": (arrayAddress, index, value) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = value > 0;
            },
            "set_array_char": (arrayAddress, index, value) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = runtime.wasmToChar(value);
            },
            "set_array_reference": (arrayAddress, index, valueAddress) => {
                const a = runtime.wasmDeref(arrayAddress);
                a[index] = runtime.wasmDeref(valueAddress);
            },
            "+_String_String": (stringRef1, stringRef2) => {
                const s1 = runtime.wasmDeref(stringRef1);
                const s2 = runtime.wasmDeref(stringRef2);
                return runtime.wasmRef(s1.concat(s2));
            },
            "+_String_numeric": (stringRef, value) => {
                const s = runtime.wasmDeref(stringRef);
                return runtime.wasmRef(s.concat(value.toString()));
            },
            "+_numeric_String": (value, stringRef) => {
                const s = runtime.wasmDeref(stringRef);
                return runtime.wasmRef(value.toString().concat(s));
            },
            "+_String_boolean": (stringRef, value) => {
                const s = runtime.wasmDeref(stringRef);
                const v = runtime.wasmBoolean(value);
                return runtime.wasmRef(s.concat(v.toString()));
            },
            "+_boolean_String": (value, stringRef) => {
                const s = runtime.wasmDeref(stringRef);
                const v = runtime.wasmBoolean(value);
                return runtime.wasmRef(v.toString().concat(s));
            },
            "+_String_char": (stringRef, value) => {
                const s = runtime.wasmDeref(stringRef);
                const v = runtime.wasmToChar(value);
                return runtime.wasmRef(s.concat(v));
            },
            "+_char_String": (value, stringRef) => {
                const s = runtime.wasmDeref(stringRef);
                const v = runtime.wasmToChar(value);
                return runtime.wasmRef(v.concat(s));
            },
            "+_String_reference": (stringRef, ref) => {
                const s = runtime.wasmDeref(stringRef);
                const o = runtime.wasmDeref(ref);
                return runtime.wasmRef(s.concat(o == null ? "null" : "Object@" + ref));
            },
            "+_reference_String": (ref, stringRef) => {
                const s = runtime.wasmDeref(stringRef);
                const o = runtime.wasmDeref(ref);
                return runtime.wasmRef((o == null ? "null" : "Object@" + ref).concat(s));
            },
        },
        imports: nativeMethods
    };
    const bytes = await readFile(file);
    const module = await WebAssembly.compile(bytes);
    const instance = new WebAssembly.Instance(module, imports);
    instance.exports["Main.main"]();
}

runModule(process.argv[2]);
