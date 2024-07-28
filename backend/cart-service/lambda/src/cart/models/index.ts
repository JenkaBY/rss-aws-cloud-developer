import { Cart } from '../entities/cart.entity';
import { CartItem } from '../entities/cart-item.entity';

export enum CartStatus {
  OPEN = 'OPEN',
  ORDERED = 'ORDERED'
}

export class Product {
  id: string;
  title: string;
  description: string;
  price: number;
}


export class CartItemDTO {
  product: Product;
  product_id: string;
  cart_id: string;
  count: number;

  public static from(dto: Partial<CartItemDTO>): CartItemDTO {
    const it = new CartItemDTO();
    it.product_id = dto.product_id;
    it.count = dto.count;
    it.product = dto.product;
    it.cart_id = dto.cart_id;
    return it;
  }

  public static fromEntity(source: CartItem): CartItemDTO {
    if (!source) {
      return null;
    }
    return CartItemDTO.from({
      product_id: source.product_id,
      count: source.count,
      product: source.product,
      cart_id: source.cart_id,
    });
  }

  public toEntity(cart_id: string): CartItem {
    const target = new CartItem();
    target.cart_id = cart_id;
    target.product_id = this.product_id;
    target.count = this.count;
    target.product = this.product;
    return target;
  }
}

export class CartDTO {
  id: string;
  user_id: string;
  created_at?: Date;
  updated_at?: Date;
  status: CartStatus;
  items: CartItemDTO[];

  public static from(dto: Partial<CartDTO>): CartDTO {
    const it = new CartDTO();
    it.id = dto.id;
    it.user_id = dto.user_id;
    it.status = dto.status;
    it.created_at = dto.created_at;
    it.updated_at = dto.updated_at;
    it.items = dto.items;
    return it;
  }

  public static fromEntity(source: Cart): CartDTO {
    if (!source) {
      return null;
    }
    return CartDTO.from({
      id: source.id,
      user_id: source.user_id,
      status: source.status,
      created_at: source.created_at,
      updated_at: source.updated_at,
      items: (source.items || [] as CartItem[]).map(item => CartItemDTO.fromEntity(item)),
    });
  }

  public toEntity(): Cart {
    const target = new Cart();
    target.id = this.id;
    target.created_at = this.created_at;
    target.updated_at = this.updated_at;
    target.user_id = this.user_id;
    target.status = this.status;
    target.items = this.items.map(item => item.toEntity(target.id));
    return target;
  }
}
