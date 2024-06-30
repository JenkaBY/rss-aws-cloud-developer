import {Config} from './config.interface';

export const environment: Config = {
  production: true,
  apiEndpoints: {
    product: 'https://6ka0pqc4k6.execute-api.eu-north-1.amazonaws.com/prod/db',
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
  accessibleUrl: "https://d1dkpi5wt9dvqq.cloudfront.net/",
  taskIdentification: 'Task 5. Turn the import-files-api on. Deployed via Java CDK.'
};
