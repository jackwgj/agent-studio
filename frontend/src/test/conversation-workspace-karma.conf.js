module.exports = function (config) {
  config.set({
    frameworks: ['jasmine'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
    ],
    browsers: ['ChromeHeadless'],
    client: { captureConsole: false },
    logLevel: config.LOG_ERROR,
    reporters: ['dots'],
    singleRun: true,
  });
};
