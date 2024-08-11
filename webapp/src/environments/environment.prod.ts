import {Config} from './config.interface';

export const environment: Config = {
  production: true,
  apiEndpoints: {
    product: 'http://jenkaby-bff-api-prod.eu-north-1.elasticbeanstalk.com/product/',
    order: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    import: 'https://zz96tej570.execute-api.eu-north-1.amazonaws.com/prod',
    bff: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    cart: 'http://jenkaby-bff-api-prod.eu-north-1.elasticbeanstalk.com/cart/',
    auth: 'http://jenkaby-bff-api-prod.eu-north-1.elasticbeanstalk.com/cart/',
  },
  apiEndpointsEnabled: {
    product: true,
    order: false,
    import: true,
    bff: false,
    cart: true,
    auth: true,
  },
  accessibleUrl: "http://d1hxiyo0kk0n80.cloudfront.net/",
  taskIdentification: 'Task 10. Backend For Frontend'
};
