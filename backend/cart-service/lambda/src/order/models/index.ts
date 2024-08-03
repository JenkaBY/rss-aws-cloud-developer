import { CartItemDTO } from '../../cart';

export class OrderDTO {
  id?: string;
  userId: string;
  cartId: string;
  items: CartItemDTO[];
  delivery: Delivery;
  status: OrderStatus;
  total: number;
}

export enum OrderStatus {
  CREATED = 'CREATED',
  SHIPPED = 'SHIPPED',
}
export type Delivery = {
  type: string;
  comments: string;
};

export class OrderInputDto {

  delivery: Delivery;
}