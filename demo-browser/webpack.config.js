const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: './src/index.js',
    output: {
        path: path.resolve(__dirname, 'build', 'dist')
    },
    plugins: [
        new CopyPlugin([
            { from: 'src/index.html' },
            { from: 'build/wasm-module/module.wasm' },
        ])
    ]
};