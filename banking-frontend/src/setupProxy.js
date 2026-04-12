const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  const GATEWAY = 'http://localhost:8090';

  const services = [
    '/auth-service',
    '/registration-service',
    '/login-service',
    '/account-service',
    '/transaction-service',
  ];

  services.forEach((service) => {
    app.use(
      service,
      createProxyMiddleware({
        target: GATEWAY,
        changeOrigin: true,
        logLevel: 'warn',
      })
    );
  });
};
