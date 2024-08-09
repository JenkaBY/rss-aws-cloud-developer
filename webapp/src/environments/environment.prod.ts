import { Config } from './config.interface';

export const environment: Config = {
  production: true,
  apiEndpoints: {
    product: 'https://wgti49plnh.execute-api.eu-north-1.amazonaws.com/prod',
    order: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    import: 'https://zz96tej570.execute-api.eu-north-1.amazonaws.com/prod',
    bff: 'https://.execute-api.eu-west-1.amazonaws.com/dev',
    cart: 'http://jenkaby-cart-api-prod.eu-north-1.elasticbeanstalk.com',
    auth: 'http://jenkaby-cart-api-prod.eu-north-1.elasticbeanstalk.com',
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
  taskIdentification: 'Task 9. Docker and AWS Elastic Beanstalk'
};
