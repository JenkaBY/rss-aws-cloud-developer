import { Column, Entity, JoinColumn, ManyToOne, PrimaryColumn } from 'typeorm';
import { Cart } from './cart.entity';
import { CartItemDTO, Product } from '../models';

@Entity('cart_items')
export class CartItem {
  @PrimaryColumn({ type: 'uuid' })
  cart_id: string;

  @ManyToOne(
    () => Cart,
    (cart) => cart.items,)
  @JoinColumn({ name: 'cart_id' })
  cart: Cart;

  @PrimaryColumn({ type: 'uuid' })
  product_id: string;

  @Column({ type: 'int' })
  count: number;

  @Column({ type: 'jsonb' })
  product: Product;

  static from(source: Partial<CartItemDTO>, cart_id: string) {
    const item = new CartItem();
    item.cart_id = cart_id;
    item.count = source.count;
    item.product_id = source.product_id || source.product?.id;
    item.product = source.product;
    return item;
  }
}