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
                test: /\.(png|svg|jpg|gif)$/,
                use: [
                    'file-loader'
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
        publicPath: "/public/"
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
        contentBase: [path.join(__dirname, "public"), path.join(__dirname, "src")],
        port: 8080,
        historyApiFallback: {
            index: 'index.html'
        }
    }
};

function isExternal(module) {
    var context = module.context;

    if (typeof context !== 'string') {
        return false;
    }

    return context.indexOf('node_modules') !== -1;
}