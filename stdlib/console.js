module.exports = function (runtime) {
    const memoryView = runtime.memoryView;
    return {
        "Console.println#int": function(arg) {
            console.log(arg)
        },
        "Console.println#float": function(arg) {
            console.log(arg)
        },
        "Console.println#boolean": function(arg) {
            console.log(arg === 0 ? 'false' : 'true')
        },
        "Console.println#int[]": function(offset) {
            const size = memoryView.getInt32(offset, true);
            const firstElement = offset + 4;
            const elementSize = 4;
            const lastElement = firstElement + size * elementSize;
            let result = [];
            for (let i = firstElement; i < lastElement; i += elementSize) {
                result.push(memoryView.getInt32(i, true));
            }
            console.log('[' + result.join(', ') + ']');
        },
        "Console.println#boolean[]": function(offset) {
            const size = memoryView.getInt32(offset, true);
            const firstElement = offset + 4;
            const lastElement = firstElement + size;
            let result = [];
            for (let i = firstElement; i < lastElement; i++) {
                result.push(memoryView.getInt8(i) === 0 ? 'false' : 'true');
            }
            console.log('[' + result.join(', ') + ']');
        },
        "Console.println#float[]": function(offset) {
            const size = memoryView.getInt32(offset, true);
            const firstElement = offset + 4;
            const elementSize = 4;
            const lastElement = firstElement + size * elementSize;
            let result = [];
            for (let i = firstElement; i < lastElement; i += elementSize) {
                result.push(memoryView.getFloat32(i, true));
            }
            console.log('[' + result.join(', ') + ']');
        },
    }
};