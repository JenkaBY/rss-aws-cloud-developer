import { Injectable } from '@angular/core';
import { CartService } from './cart.service';
import { ProductsService } from '../products/products.service';
import { Observable } from 'rxjs';
import { ProductCheckout } from '../products/product.interface';
import { map, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class CheckoutService {
  constructor(
    private readonly cartService: CartService,
    private readonly productsService: ProductsService
  ) {}

  getProductsForCheckout(): Observable<ProductCheckout[]> {
    return this.cartService.getCurrentCart().pipe(
      switchMap((cart) =>
        this.productsService.getProductsForCheckout(cart.items.map(i => i.product.id)).pipe(
          map((products) =>
            products.map((product) => ({
              ...product,
              orderedCount: cart.items.find(item => item.product.id === product.id)?.count || 0,
              totalPrice: +((cart.items.find(item => item.product.id === product.id)?.count || 0) * product.price).toFixed(2),
            }))
          )
        )
      )
    );
  }
}
