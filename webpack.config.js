const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  target: 'web',
  entry: path.resolve('src/index.tsx'),
  mode: 'development',
  output: {
    filename: 'index.js',
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: 'babel-loader',
      },
      {
        test: /\.js$/,
        use: ["source-map-loader"],
        enforce: "pre"
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.js'],
    modules: [path.resolve('src'), path.resolve('node_modules')],
  },
  plugins: [
    new HtmlWebpackPlugin(),
  ],
}
