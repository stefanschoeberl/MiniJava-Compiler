module.exports = runtime => {
    return {
        'new_array_numeric': size => {
            return runtime.wasmRef(Array(size).fill(0));
        },
        'new_array_boolean': size => {
            return runtime.wasmRef(Array(size).fill(false));
        },
        'new_array_char': size => {
            return runtime.wasmRef(Array(size).fill('\0'));
        },
        'new_array_reference': size => {
            return runtime.wasmRef(Array(size).fill(null));
        },
        'array_length': arrayRef => {
            return runtime.wasmDeref(arrayRef).length;
        },
        'get_array_numeric': (arrayRef, index) => {
            const a = runtime.wasmDeref(arrayRef);
            return a[index];
        },
        'get_array_boolean': (arrayRef, index) => {
            const a = runtime.wasmDeref(arrayRef);
            return a[index] ? 1 : 0;
        },
        'get_array_char': (arrayRef, index) => {
            const a = runtime.wasmDeref(arrayRef);
            return runtime.charToWasm(a[index]);
        },
        'get_array_reference': (arrayRef, index) => {
            const a = runtime.wasmDeref(arrayRef);
            return runtime.wasmRef(a[index]);
        },
        'set_array_numeric': (arrayRef, index, value) => {
            const a = runtime.wasmDeref(arrayRef);
            a[index] = value;
        },
        'set_array_boolean': (arrayRef, index, value) => {
            const a = runtime.wasmDeref(arrayRef);
            a[index] = value > 0;
        },
        'set_array_char': (arrayRef, index, value) => {
            const a = runtime.wasmDeref(arrayRef);
            a[index] = runtime.wasmToChar(value);
        },
        'set_array_reference': (arrayRef, index, valueRef) => {
            const a = runtime.wasmDeref(arrayRef);
            a[index] = runtime.wasmDeref(valueRef);
        },
        '+_String_String': (stringRef1, stringRef2) => {
            const s1 = runtime.wasmDeref(stringRef1);
            const s2 = runtime.wasmDeref(stringRef2);
            return runtime.wasmRefType(s1.concat(s2), 'String');
        },
        '+_String_numeric': (stringRef, value) => {
            const s = runtime.wasmDeref(stringRef);
            return runtime.wasmRefType(s.concat(value.toString()), 'String');
        },
        '+_numeric_String': (value, stringRef) => {
            const s = runtime.wasmDeref(stringRef);
            return runtime.wasmRefType(value.toString().concat(s), 'String');
        },
        '+_String_boolean': (stringRef, value) => {
            const s = runtime.wasmDeref(stringRef);
            const v = runtime.wasmBoolean(value);
            return runtime.wasmRefType(s.concat(v.toString()), 'String');
        },
        '+_boolean_String': (value, stringRef) => {
            const s = runtime.wasmDeref(stringRef);
            const v = runtime.wasmBoolean(value);
            return runtime.wasmRefType(v.toString().concat(s), 'String');
        },
        '+_String_char': (stringRef, value) => {
            const s = runtime.wasmDeref(stringRef);
            const v = runtime.wasmToChar(value);
            return runtime.wasmRefType(s.concat(v), 'String');
        },
        '+_char_String': (value, stringRef) => {
            const s = runtime.wasmDeref(stringRef);
            const v = runtime.wasmToChar(value);
            return runtime.wasmRefType(v.concat(s), 'String');
        },
        '+_String_reference': (stringRef, ref) => {
            const s = runtime.wasmDeref(stringRef);
            const o = runtime.wasmDeref(ref);
            return runtime.wasmRefType(s.concat(o == null ? 'null' : 'Object@' + ref), 'String');
        },
        '+_reference_String': (ref, stringRef) => {
            const s = runtime.wasmDeref(stringRef);
            const o = runtime.wasmDeref(ref);
            return runtime.wasmRefType((o == null ? 'null' : 'Object@' + ref).concat(s), 'String');
        },
    }
};