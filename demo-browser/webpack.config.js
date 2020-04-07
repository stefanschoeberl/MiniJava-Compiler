const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
    entry: ['./src/index.js', './src/style.css'],
    output: {
        path: path.resolve(__dirname, 'build', 'dist')
    },
    plugins: [
        new CopyPlugin([
            { from: 'src/index.html' },
            { from: 'build/wasm-module/module.wasm' },
        ]),
        new MiniCssExtractPlugin()
    ],
    module: {
        rules: [
            {
                test: /\.css$/,
                use:[MiniCssExtractPlugin.loader, 'css-loader']
            }
        ]
    }
};