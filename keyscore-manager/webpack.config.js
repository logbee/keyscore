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
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },
            {
                test: /\.(png|jpg|gif)$/,
                use: [
                    'file-loader'
                ]
            },
            {
                test: /\.svg$/,
                use: [
                    'svg-loader'
                ]
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
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendors',
            minChunks: function(module) {
                return isExternal(module);
            },
            filename: 'vendor.bundle.js'
        })
    ],
    devServer: {
        contentBase: [
            path.join(__dirname, "conf"),
            path.join(__dirname, "public")
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