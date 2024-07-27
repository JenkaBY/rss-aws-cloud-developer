import { Module } from '@nestjs/common';

import { CartController } from './cart.controller';
import { CartService } from './services';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Cart, CartItem } from './entities';
import { OrderModule } from '../order/order.module';


@Module({
  // TODO remove OrderModule
  imports: [TypeOrmModule.forFeature([Cart, CartItem]), OrderModule],
  providers: [CartService],
  controllers: [CartController],
})
export class CartModule {
}
