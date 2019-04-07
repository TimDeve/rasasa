const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyPlugin = require('copy-webpack-plugin')
const webpack = require('webpack')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')

const isDev = process.env.NODE_ENV !== 'production'

module.exports = {
  target: 'web',
  entry: {
    index: path.resolve('src/index.tsx'),
  },
  mode: isDev ? 'development' : 'production',
  devServer: {
    port: 8090,
    hot: true,
    historyApiFallback: true,
    host: '0.0.0.0',
    proxy: [
      {
        context: ['/api/**', '!**/read'],
        target: 'http://localhost:8091',
        pathRewrite: { '^/api': '' },
      },
      {
        context: ['/api/v0/read'],
        target: 'http://localhost:8092',
        pathRewrite: { '^/api': '' },
      },
    ],
  },
  output: {
    filename: '[name].js',
    publicPath: '/',
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: ['babel-loader', 'react-hot-loader/webpack'],
      },
      {
        test: /\.jsx?$/,
        use: ['source-map-loader'],
        enforce: 'pre',
      },
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            options: {
              modules: true,
              localIdentName: '[name]__[local]___[hash:base64:5]',
              camelCase: true,
              sourceMap: true,
            },
          },
          {
            loader: 'sass-loader',
            options: {
              sourceMap: true,
            },
          },
        ],
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js', '.jsx'],
    modules: [path.resolve('src'), path.resolve('node_modules')],
    alias: {
      'react-dom': '@hot-loader/react-dom',
    },
  },
  plugins: [
    new HtmlWebpackPlugin({ template: path.resolve('src/index.html') }),
    new webpack.HotModuleReplacementPlugin(),
    new CopyPlugin([{ from: 'static' }, { from: 'src/sw.js' }]),
    new MiniCssExtractPlugin({
      filename: '[name].css',
      chunkFilename: '[id].css',
    }),
  ],
}
