import { Injectable } from '@nestjs/common';

import { v4 } from 'uuid';

import { CartDTO, CartStatus } from '../models';
import { Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { Cart, CartItem } from '../entities';

@Injectable()
export class CartService {

  constructor(@InjectRepository(Cart) private readonly cartRepo: Repository<Cart>,
  ) {
  }

  public async findByUserId(userId: string): Promise<CartDTO> {
    return this.cartRepo.findOneBy({
      user_id: userId,
      status: CartStatus.OPEN,
    }).then(entity => {
      console.log('Found cart:', entity);
      return CartDTO.fromEntity(entity);
    });
  }

  public async createByUserId(userId: string): Promise<CartDTO> {
    const id = v4();
    const userCart: Cart = {
      id: id,
      user_id: userId,
      status: CartStatus.OPEN,
      items: [],
    };
    return this.cartRepo.save(userCart)
      .then(entity => CartDTO.fromEntity(entity));
  }

  public async findOrCreateByUserId(userId: string): Promise<CartDTO> {
    const userCart = await this.findByUserId(userId);

    if (userCart) {
      return userCart;
    }
    console.log('Not found a cart for user_id', userId);
    return this.createByUserId(userId);
  }

  public async updateByUserId(userId: string, { items }: CartDTO): Promise<CartDTO> {
    const cartDTO = this.findOrCreateByUserId(userId);

    const updatedCart = cartDTO.then(cart => {
      const updatedItems = items.map(item => CartItem.from(item, cart.id));
      return { ...cart, ...{ items: updatedItems } } as Cart;
    });
    return this.cartRepo.manager.transaction(async entityManager => {
      const cart = await updatedCart;
      await entityManager.delete(CartItem, { cart_id: cart.id });
      const saved = await entityManager.save(Cart, cart);
      return CartDTO.fromEntity(saved);
    });
  }

  public async removeByUserId(userId: string): Promise<void> {
    await this.cartRepo.findOneBy({
      user_id: userId,
      status: CartStatus.OPEN,
    }).then(cart =>
      this.cartRepo.manager.transaction(async (entityManager) => {
        await entityManager.remove(CartItem, cart.items);
        await entityManager.remove(Cart, cart);
      }));
  }
}
