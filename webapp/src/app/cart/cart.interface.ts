import { CartItem } from './cart-item.interface';

export interface Cart {
  id: string;
  status: string;
  user_id: string;
  items: CartItem[];
}

export interface CartWithTotal {
  cart: Cart,
  total: number
}
