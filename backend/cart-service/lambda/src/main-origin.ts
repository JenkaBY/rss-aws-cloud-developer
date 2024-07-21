import { NestFactory } from '@nestjs/core';

import helmet from 'helmet';

import { AppModule } from './app.module';

const port = process.env.PORT || 4000;

async function bootstrap() {
    const app = await NestFactory.create(AppModule);

    app.enableCors({
        origin: (req, callback) => callback(null, true),
    });
    app.use(helmet());

    await app.listen(port);
    // TODO delete because of nest solution usage
    // AppDataSource.initialize()
    //   .then(async () => {
    //       console.log("DB initialized")
    //   })
    //   .catch((error) => console.log("Error: ", error))
}
bootstrap().then(() => {
    console.log('App is running on %s port', port);
});
