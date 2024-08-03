import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';
import { CartApiService } from './cart-api.service';
import { Product } from '../products/product.interface';
import { Cart } from './cart.interface';
import { CartItem } from './cart-item.interface';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  currentCart?: Cart;
  #cartSource = new ReplaySubject<Cart>(1);

  totalInCart$: Observable<number> = this.#cartSource.pipe(
    map((cart) => {
      const items = cart.items;

      if (!items || !items.length) {
        return 0;
      }

      return items.reduce((acc, item) => acc + item.count, 0);
    }),
    shareReplay({
      refCount: true,
      bufferSize: 1,
    }),
  );

  constructor(private readonly cartApiService: CartApiService) {
  }

  init() {
    this.cartApiService.getCart().subscribe(cartWithTotal => {
      this.currentCart = cartWithTotal.cart;
      this.#cartSource.next(cartWithTotal.cart);
    });
  }

  addItem(product: Product): void {
    this.updateCount(product, 1);
  }

  removeItem(product: Product): void {
    this.updateCount(product, -1);
  }

  empty(): void {
    // this.#cartSource.next({});
    //   TODO implement delete cart.
  }

  private updateCount(product: Product, type: 1 | -1): void {
    const cartItem = this.currentCart?.items.find(item => item.product_id === product.id);
    if (!cartItem && type === 1) {
      const cartToUpdate = { ...this.currentCart } as Cart;
      cartToUpdate.items.push({
        count: 1,
        product: product,
        cart_id: cartToUpdate.id,
        product_id: product.id,
      });
      this.cartApiService.updateCart(cartToUpdate)
        .pipe(
          shareReplay({
            refCount: true,
            bufferSize: 1,
          }),
        )
        .subscribe(updatedCart => this.#cartSource.next(updatedCart.cart));
      return;
    } else if (!cartItem && type === -1) {
      console.warn('Skipping. Impossible to decrement missing cart item.');
      return;
    }
    const newCount = (cartItem?.count || 0) + type;
    const cartToUpdate = { ...this.currentCart } as Cart;
    const index = cartToUpdate.items.indexOf(cartItem as CartItem);
    if (newCount === 0) {
      cartToUpdate.items.splice(index, 1);
    } else {
      const cartItemToUpdate = {  ...cartItem, count: newCount } as CartItem;
      cartToUpdate.items[index] = cartItemToUpdate;
    }
    this.cartApiService.updateCart(cartToUpdate)
      .subscribe(updatedCart => this.#cartSource.next(updatedCart.cart));
  }

  getCurrentCart(): Observable<Cart> {
    return this.#cartSource.asObservable();
  }

  checkout(payload: any): Observable<any> {
    return this.cartApiService.checkout(payload);
  }
}
