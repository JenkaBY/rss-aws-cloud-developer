import { CartDTO, CartItemDTO } from '../models';

/**
 * @param {CartDTO} cart
 * @returns {number}
 */
export function calculateCartTotal(cart: CartDTO): number {
  console.log("Calculate Cart Total: ", cart)
  return cart ? cart.items.reduce((acc: number, { product: { price }, count }: CartItemDTO) => {
    return acc += price * count;
  }, 0) : 0;
}
