import {Config} from './config.interface';

export const environment: Config = {
  production: true,
  apiEndpoints: {
    product: 'https://wgti49plnh.execute-api.eu-north-1.amazonaws.com/prod/',
    order: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    import: 'https://zz96tej570.execute-api.eu-north-1.amazonaws.com/prod',
    bff: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    cart: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
  },
  apiEndpointsEnabled: {
    product: true,
    order: false,
    import: true,
    bff: false,
    cart: false,
  },
  accessibleUrl: "https://d1hxiyo0kk0n80.cloudfront.net/",
  taskIdentification: 'Task 6. Async communication. Deployed via Java CDK.'
};
