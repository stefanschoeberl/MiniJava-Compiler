const fs = require('fs');

// https://webassembly.org/getting-started/js-api/

function runFile(file) {
    const imports = {imports: {
        println: arg => console.log(arg)
    }};
    fs.readFile(file, (err, bytes) => {
        WebAssembly.compile(bytes)
            .then(m => new WebAssembly.Instance(m, imports))
            .then(instance => instance.exports.main());
    });
}

runFile(process.argv[2]);
