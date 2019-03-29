const path = require('path');
// KS:M webpack.config.js
const custom = require('../webpack.config.js');

module.exports = async ({ config, mode }) => {
    return { ...config, module: custom.module };
};
