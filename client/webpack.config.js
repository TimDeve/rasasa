const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyPlugin = require('copy-webpack-plugin')
const webpack = require('webpack')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const { HotModuleReplacementFilterPlugin } = require('hmr-filter-webpack-plugin')
const ManifestPlugin = require('webpack-manifest-plugin')
const { v4: uuidv4 } = require('uuid')

const isDev = process.env.NODE_ENV !== 'production'

module.exports = {
  target: 'web',
  stats: isDev
    ? {
        all: false,
        errors: true,
        warnings: true,
        builtAt: true,
      }
    : 'normal',
  entry: {
    index: path.resolve('src/index.tsx'),
  },
  mode: isDev ? 'development' : 'production',
  devServer: {
    port: 8089,
    hot: true,
    historyApiFallback: true,
    host: '0.0.0.0',
    proxy: [
      {
        context: ['/api/**'],
        target: 'http://localhost:8090',
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
              modules: {
                localIdentName: '[name]__[local]___[hash:base64:5]',
              },
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
    new HotModuleReplacementFilterPlugin(compilation => {
      const { name } = compilation.compiler
      return name && name.includes('worker')
    }),
    new webpack.HotModuleReplacementPlugin(),
    new CopyPlugin({ patterns: [{ from: 'static' }] }),
    new MiniCssExtractPlugin({
      filename: '[name].css',
      chunkFilename: '[id].css',
    }),
    new ManifestPlugin({
      fileName: 'assets-manifest.js',
      serialize(obj) {
        return `
          self.__precacheManifest = (self.__precacheManifest || []).concat([
            ${Object.values(obj)
              .map(file => `{"url": "${file}", "revision": "${uuidv4()}"}`)
              .join(',')}
          ]);
        `
      },
    }),
  ],
}
