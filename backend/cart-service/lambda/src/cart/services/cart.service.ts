import { Injectable } from '@nestjs/common';

import { v4 } from 'uuid';

import { CartDTO, CartStatus } from '../models';
import { Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { Cart, CartItem } from '../entities';

@Injectable()
export class CartService {
  private userCarts: Record<string, CartDTO> = {};

  constructor(@InjectRepository(Cart) private readonly cartRepo: Repository<Cart>,
  ) {
  }

  public async _findByUserId(userId: string): Promise<CartDTO> {
    return this.cartRepo.findOneBy({
      user_id: userId,
      status: CartStatus.OPEN,
    }).then(entity => {
      console.log('Found cart:', entity);
      return CartDTO.fromEntity(entity);
    });
  }

  findByUserId(userId: string): CartDTO {
    return this.userCarts[userId];
  }

  _createByUserId(userId: string): Promise<CartDTO> {
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

  createByUserId(userId: string) {
    const id = v4();
    const now = Date.now();
    const userCart: CartDTO = CartDTO.from({
      id,
      user_id: userId,
      // created_at: now,
      // updated_at: now,
      status: CartStatus.OPEN,
      items: [],
    });

    this.userCarts[userId] = userCart;

    return userCart;
  }

  findOrCreateByUserId(userId: string): CartDTO {
    const userCart = this.findByUserId(userId);

    if (userCart) {
      return userCart;
    }

    return this.createByUserId(userId);
  }

  async _findOrCreateByUserId(userId: string): Promise<CartDTO> {
    const userCart = await this._findByUserId(userId);

    if (userCart) {
      return userCart;
    }
    console.log('Not found a cart for user_id', userId);
    return this._createByUserId(userId);
  }

  updateByUserId(userId: string, { items }: CartDTO): CartDTO | any {
    const { id, ...rest } = this.findOrCreateByUserId(userId);

    const updatedCart = {
      id,
      ...rest,
      items: [...items],
    };

    this.userCarts[userId] = { ...updatedCart } as CartDTO;

    return { ...updatedCart };
  }

  async _updateByUserId(userId: string, { items }: CartDTO): Promise<CartDTO> {
    const cartDTO = this._findOrCreateByUserId(userId);

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

  removeByUserId(userId: string): void {
    this.userCarts[userId] = null;
  }

  async _removeByUserId(userId: string): Promise<void> {
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
