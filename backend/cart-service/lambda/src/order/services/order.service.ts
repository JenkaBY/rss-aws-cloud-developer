import { Injectable } from '@nestjs/common';
import { v4 } from 'uuid';

import { OrderDTO, OrderStatus } from '../models';
import { InjectRepository } from '@nestjs/typeorm';
import { EntityManager, Repository } from 'typeorm';
import { Order } from '../entities/order.entity';
import { calculateCartTotal } from '../../cart/models-rules';
import { Cart, CartStatus } from '../../cart';

@Injectable()
export class OrderService {
  private entityManager: EntityManager;

  constructor(@InjectRepository(Order) private readonly orderRepo: Repository<Order>) {
    this.entityManager = orderRepo.manager;
  }

  async getOrders(): Promise<any[]> {
    return await this.orderRepo.find({
      order: { userId: 'ASC' },
    });
  }

  async checkout(data: any): Promise<OrderDTO> {
    console.log('Checkout Service checkout input', data);
    const { userId, cart, delivery } = data;
    const { id: cartId, items } = cart;
    const total = calculateCartTotal(cart);

    const order = await this.entityManager.transaction(async entityManager => {
      const order = {
        id: v4(),
        userId,
        delivery,
        total,
        comments: delivery.comments,
        cart: {
          id: cartId,
        } as Partial<Cart>,
        status: OrderStatus.CREATED,
      } as Order;
      const orderDto = await entityManager.save(Order, order)
        .then(created => {
          return { ...created, items, cartId: created.cart.id, userId, total: created.total } as OrderDTO;
        });

      await entityManager.update(Cart,
        {
          id: cartId,
        },
        {
          status: CartStatus.ORDERED,
        });
      return orderDto;
    });
    return order;
  }


}
