import { Injectable } from '@angular/core';

import { EMPTY, Observable } from 'rxjs';

import { ApiService } from '../core/api.service';
import { Cart, CartWithTotal } from './cart.interface';
import { BackendResponse } from '../core/response/backend-response.interface';
import { map, tap } from 'rxjs/operators';


@Injectable({
  providedIn: 'root',
})
export class CartApiService extends ApiService {

  getCart(): Observable<CartWithTotal> {
    if (!this.endpointEnabled('cart')) {
      console.warn(
        'Endpoint "cart" is disabled. To enable change your environment.ts config',
      );
      return EMPTY;
    }
    console.log('Retrieve cart');
    const headersAuth = this.getAuthHeader();
    const url = this.getUrl('cart', '/api/profile/cart');
    return this.http.get<BackendResponse<CartWithTotal>>(url, {
      headers: headersAuth,
    }).pipe(
      map(res => res.data),
    );
  }

  updateCart(cart: Cart): Observable<CartWithTotal> {
    if (!this.endpointEnabled('cart')) {
      console.warn(
        'Endpoint "cart" is disabled. To enable change your environment.ts config',
      );
      return EMPTY;
    }
    const authHeader = this.getAuthHeader();
    const url = this.getUrl('cart', '/api/profile/cart');
    return this.http.put<BackendResponse<CartWithTotal>>(url, cart, {
      headers: authHeader,
    }).pipe(
      map(res => res.data),
    );
  }

  private getAuthHeader(): any {
    const authorizationToken = localStorage.getItem('Authorization');
    console.log(`Found token: ${authorizationToken}`);
    let headersAuth: any = {};
    if (authorizationToken) {
      headersAuth['Authorization'] = authorizationToken;
    }
    return headersAuth;
  }

  checkout(payload: any): Observable<any> {
    if (!this.endpointEnabled('cart')) {
      console.warn(
        'Endpoint "cart" is disabled. To enable change your environment.ts config',
      );
      return EMPTY;
    }
    const authHeader = this.getAuthHeader();
    const url = this.getUrl('cart', '/api/profile/cart/checkout');
    return this.http.post(url, payload, {
      headers: authHeader,
    }).pipe(
      tap(res => console.log('Checkout order', res),
        fail => console.log('Fail checkout', fail)),
    );
  }
}
