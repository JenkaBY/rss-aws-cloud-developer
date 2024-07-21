import { DataSource } from 'typeorm';
import { CartDTO, CartItemDTO } from './cart';
//  this is required by orm
import 'reflect-metadata';

export const AppDataSource = new DataSource({
  type: 'postgres',
  host: process.env.PG_URL || 'localhost',
  port: +process.env.PG_DBPORT || 5432,
  username: process.env.PG_USER || 'postgres',
  password: process.env.PG_PASSWORD || 'postgres',
  database: process.env.PG_DBNAME || 'cartservice',
  synchronize: false,
  logging: true,
  entities: [CartItemDTO, CartDTO],
  subscribers: [],
  migrations: [],
});