const path = require('path');
const webpack = require("webpack");
const helpers = require('./helpers');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');

let modules = [
    './modules/keyscore-manager-material/index.ts',
    './modules/keyscore-manager-models/index.ts',
    './modules/keyscore-manager-pipeline-parameters/index.ts',
    './modules/keyscore-manager-test-fixtures/index.ts'
];

module.exports = {
    entry: {
        app: ['babel-polyfill', './src/main.ts',...modules]
    },
    mode: 'development', //devtool: 'source-map', // Slows down the build
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: ['awesome-typescript-loader','angular2-template-loader?keepUrl=true'],
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
                test: /\.scss$/,
                use: [
                    {
                        loader: 'style-loader'
                    },
                    {
                        loader: 'css-loader'
                    },
                    {
                        loader: 'sass-loader',
                        options:{
                            includePaths:['./modules']
                        }
                    }
                ]
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
