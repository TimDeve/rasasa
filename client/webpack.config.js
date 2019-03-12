const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const webpack = require('webpack')

module.exports = {
  target: 'web',
  entry: path.resolve('src/index.tsx'),
  mode: 'development',
  devServer: {
    port: 8090,
    hot: true,
    historyApiFallback: true,
    host: '0.0.0.0',
    proxy: {
      '/api': { target: 'http://localhost:8091', pathRewrite: { '^/api': '' } },
    },
  },
  output: {
    filename: 'index.js',
  },
  module: {
    rules: [
      {
        test: /\.tsx$/,
        use: ['babel-loader', 'react-hot-loader/webpack'],
      },
      {
        test: /\.js$/,
        use: ['source-map-loader'],
        enforce: 'pre',
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.js'],
    modules: [path.resolve('src'), path.resolve('node_modules')],
    alias: {
      'react-dom': '@hot-loader/react-dom',
    },
  },
  plugins: [
    new HtmlWebpackPlugin({ template: path.resolve('src/index.html') }),
    new webpack.HotModuleReplacementPlugin(),
  ],
}
