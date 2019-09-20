const path = require('path');
const webpack = require("webpack");
const helpers = require('./helpers');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const CircularDependencyPlugin = require('circular-dependency-plugin');


module.exports = {
    entry: {
        app: ['babel-polyfill', './src/main.ts']
    },
    mode: 'development', //devtool: 'source-map', // Slows down the build
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: ['awesome-typescript-loader','angular2-template-loader','angular-router-loader'],
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
                        customAttrSurround: [[/#/, /(?:)/], [/\*/, /(?:)/], [/\[?\(?/, /(?:)/]],
                        customAttrAssign: [/\)?\]?=/]
                    }
                }]
            },
            {
                test: /\.(scss|sass)$/,
                use: [
                    { loader: 'style-loader', options: { sourceMap: true } },
                    { loader: 'css-loader', options: { sourceMap: true } },
                    { loader: 'sass-loader', options: { sourceMap: true } }
                ],
                include: helpers.root('src', 'assets')
            },
            {
                test: /\.(scss|sass)$/,
                use: [
                    'to-string-loader',
                    { loader: 'css-loader', options: { sourceMap: true } },
                    { loader: 'sass-loader', options: { sourceMap: true } }
                ],
                include: [helpers.root('src', 'app'),helpers.root('modules')]
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },

            {
                test: /\.(png|jpg|jpeg|gif|svg|ttf|woff2|woff|eot)$/,
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
        extensions: [".tsx", ".ts", ".js"],
            plugins: [new TsconfigPathsPlugin({configFile:"./tsconfig.json"})]
    },
    output: {
        filename: 'keyscore.bundle.js',
        path: path.resolve(__dirname, 'build/webpack'),
        publicPath: "/"
    },
    optimization: {
        runtimeChunk: "single",
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: "vendors",
                    chunks: "all"
                }
            }
        }
    },
    plugins: [
        new CircularDependencyPlugin({
            // exclude detection of files based on a RegExp
            exclude: /a\.js|node_modules/,
            // add errors to webpack instead of warnings
            failOnError: true,
            // allow import cycles that include an asyncronous import,
            // e.g. via import(/* webpackMode: "weak" */ './file.js')
            allowAsyncCycles: false,
            // set the current working directory for displaying module paths
            cwd: process.cwd(),
        }),
        new webpack.ProvidePlugin({
            "window.jQuery": "jquery"
        }),
        new webpack.ProvidePlugin({
            jQuery: 'jquery',
            $: 'jquery',
            jquery: 'jquery'
        }),
        new webpack.ContextReplacementPlugin(
            /\@angular(\\|\/)core(\\|\/)fesm5/,
            helpers.root('./src'),
            {}
        )],
    devServer: {
        contentBase: [
            path.join(__dirname, "/public"),
            path.join(__dirname, "../media"),
            path.join(__dirname, "/conf")
        ],
        port: 8080,
        host: "0.0.0.0",
        quiet: false,
        noInfo: false,
        historyApiFallback: true,
        watchContentBase: true
    }
};
