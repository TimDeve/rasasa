declare module 'service-worker-loader!*' {
  const register: import('service-worker-loader/types').ServiceWorkerRegister;
  const scriptUrl: import('service-worker-loader/types').ScriptUrl;
  const ServiceWorkerNoSupportError: import('service-worker-loader/types').ServiceWorkerNoSupportError;
  export default register;
  export {
    scriptUrl,
    ServiceWorkerNoSupportError
  };
}
