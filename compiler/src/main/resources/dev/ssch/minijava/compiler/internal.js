module.exports = runtime => {
    return {
        "new_array_numeric": size => {
            return runtime.wasmRef(Array(size).fill(0));
        },
        "new_array_boolean": size => {
            return runtime.wasmRef(Array(size).fill(false));
        },
        "new_array_char": size => {
            return runtime.wasmRef(Array(size).fill('\0'));
        },
        "new_array_reference": size => {
            return runtime.wasmRef(Array(size).fill(null));
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
    }
};