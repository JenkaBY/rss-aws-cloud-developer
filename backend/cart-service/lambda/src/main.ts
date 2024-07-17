import { NestFactory } from '@nestjs/core';
import serverlessExpress from '@codegenie/serverless-express';
import helmet from 'helmet';
import { Callback, Context, Handler } from 'aws-lambda';
import { AppModule } from './app.module';

const port = process.env.PORT || 4000;
let server: Handler;

async function bootstrap() {
    const app = await NestFactory.create(AppModule);

    app.enableCors({
        origin: (req, callback) => callback(null, true),
    });
    app.use(helmet());

    await app.listen(port);

    const expressApp = app.getHttpAdapter().getInstance();
    return serverlessExpress({app: expressApp});
}

const handler: Handler = async (
    event: any,
    context: Context,
    callback: Callback,
) => {
    console.log("incoming event", event)
    server = server ?? (await bootstrap());
    return server(event, context, callback);
};

module.exports.handler = handler;