import {Config} from './config.interface';

export const environment: Config = {
  production: true,
  apiEndpoints: {
    product: 'https://blt00b0cx1.execute-api.eu-north-1.amazonaws.com/prod',
    order: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    import: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    bff: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    cart: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
  },
  apiEndpointsEnabled: {
    product: true,
    order: false,
    import: false,
    bff: false,
    cart: false,
  },
  accessibleUrl: "https://dzjk01dufwe4q.cloudfront.net/",
  taskIdentification: 'Task 3. The product API and configurations were created programmatically via Java CDK.'
};
