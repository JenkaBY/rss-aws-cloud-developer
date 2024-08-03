import { TypeOrmModuleOptions } from '@nestjs/typeorm';
import { TlsOptions } from 'tls';


require('dotenv').config();


class ConfigService {

  constructor(private env: { [k: string]: string | undefined }) {
  }

  private getValue(key: string, throwOnMissing = true): string {
    const value = this.env[key];
    if (!value && throwOnMissing) {
      throw new Error(`config error - missing env.${key}`);
    }

    return value;
  }

  public ensureValues(keys: string[]) {
    keys.forEach(k => this.getValue(k, true));
    return this;
  }

  public getPort() {
    return this.getValue('PG_DBPORT', true);
  }

  public isProduction() {
    const mode = this.getValue('ENV', false);
    return mode === 'PROD';
  }

  public getTypeOrmConfig(): TypeOrmModuleOptions {
    return {
      type: 'postgres',
      host: this.getValue('PG_URL'),
      port: parseInt(this.getValue('PG_DBPORT')),
      username: this.getValue('PG_USER'),
      password: this.getValue('PG_PASSWORD'),
      database: this.getValue('PG_DBNAME'),

      entities: [__dirname + '/**/*.entity{.ts,.js}'],

      // migrationsTableName: 'migration',

      // migrations: ['src/migration/*.ts'],
      synchronize: false,
      manualInitialization: false,
      logging: false,
      autoLoadEntities: true,
      ssl: this.getSslConfig(),
    };
  }

  private getSslConfig(): boolean | TlsOptions {
    // TODO Enable ssl for PROD
    // return this.isProduction() ? this.sslConfig(): false;
    return false;
  }

  private sslConfig():TlsOptions {
    return {
      rejectUnauthorized: true,
      // ca: fs.readFileSync('/pathto/rds-ca-cert.pem').toString(),
    }
  }
}

const configService = new ConfigService(process.env)
  .ensureValues([
    'PG_URL',
    'PG_DBPORT',
    'PG_USER',
    'PG_PASSWORD',
    'PG_DBNAME',
  ]);

export { configService };