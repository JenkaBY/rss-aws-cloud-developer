### How to

Due to dependency between stacks, a deploying order is important. The Product API should be deployed first.
Run from the root of project the following command:
1. Deploy Product API and UI.  
``` cd /backend/product-service/deploy && cdk deploy ```
2. Deploy the Authorization Service
   ``` cd /backend/authorization-service/deploy && cdk deploy ```
3. Then deploy the Import API by running from the root folder of the project:
``` cd /backend/import-service/deploy && cdk deploy ```

### Links

The working application is available [here](https://d1hxiyo0kk0n80.cloudfront.net/)