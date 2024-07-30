import { Product } from '../products/product.interface';

export interface CartItem {
  cart_id: string;
  count: number;
  product_id: string;
  product: Product;
}
