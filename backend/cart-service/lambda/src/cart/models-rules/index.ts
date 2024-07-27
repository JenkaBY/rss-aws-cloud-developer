import { CartDTO, CartItemDTO } from '../models';

/**
 * @param {CartDTO} cart
 * @returns {number}
 */
export function calculateCartTotal(cart: CartDTO): number {
  return cart ? +Number.parseFloat('' + cart.items.reduce((acc: number, { product: { price }, count }: CartItemDTO) => {
    return acc += price * count;
  }, 0)).toFixed(2) : 0;
}
