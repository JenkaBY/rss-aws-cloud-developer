### How to
Cart Api deploy:
``` cd /backend/cart-service/deploy && cdk deploy ```

Due to dependency between stacks, a deploying order is important. The Product API should be deployed first.
Run from the root of project the following command:
1. Deploy Product API and UI.  
``` cd /backend/product-service/deploy && cdk deploy ```
2. Deploy the Authorization Service
``` cd /backend/authorization-service/deploy && cdk deploy ```
3. Then deploy the Import API by running from the root folder of the project:
``` cd /backend/import-service/deploy && cdk deploy ```
4. Then deploy Postgres DB by running from the root folder of the project:
``` cd /backend/cart-service/deploy && cdk deploy ```
5. Then deploy Cart Service by running from the root folder of the project:
``` cd /backend/cart-service/lambda && eb init ... && eb create ... && eb deploy ... ```
6. Then deploy the bff service by running from the root folder of the project:
``` cd /backend/bff-service/app && eb init ... && eb create ... && eb deploy ...```

### Links

The working application is available [here](https://d1hxiyo0kk0n80.cloudfront.net/)