const path = require('path');
const webpack = require("webpack");

module.exports = {
    entry: {
        app: ['./src/main.ts']
    },
    // devtool: 'inline-source-map', // Slows down the build
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            },
            {
                test: /\.html$/,
                use: [{
                    loader: 'html-loader',
                    options: {
                        minimize: true,
                        removeAttributeQuotes: false,
                        caseSensitive: true,
                        customAttrSurround: [ [/#/, /(?:)/], [/\*/, /(?:)/], [/\[?\(?/, /(?:)/] ],
                        customAttrAssign: [ /\)?\]?=/ ]
                    }
                }]
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },
            {
                test: /\.(png|jpg|jpeg|gif|svg)$/,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit: 8000, // Convert images < 8kb to base64 strings
                        name: 'assets/[hash]-[name].[ext]'
                    }
                }]
            }
        ]
    },
    resolve: {
        extensions: [ ".tsx", ".ts", ".js" ]
    },
    output: {
        filename: 'keyscore.bundle.js',
        path: path.resolve(__dirname, 'build/webpack'),
        publicPath: "/"
    },
    plugins: [
        new webpack.ProvidePlugin({
            "window.jQuery": "jquery"
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendors',
            minChunks: function(module) {
                return isExternal(module);
            },
            filename: 'vendor.bundle.js'
        }),
        new webpack.ProvidePlugin({
            jQuery: 'jquery',
            $: 'jquery',
            jquery: 'jquery'
        }),
        new webpack.ProvidePlugin({
            Blockly:'node-blockly/browser.js'
        })
    ],
    devServer: {
        contentBase: [
            path.join(__dirname, "conf"),
            path.join(__dirname, "public"),
            path.join(__dirname, "../media")
        ],
        port: 8080,
        quiet: false,
        noInfo: false,
        historyApiFallback: true
    }
};

function isExternal(module) {
    var context = module.context;

    if (typeof context !== 'string') {
        return false;
    }

    return context.indexOf('node_modules') !== -1;
}