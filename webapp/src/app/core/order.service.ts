import { Injectable } from '@angular/core';

import { EMPTY, Observable } from 'rxjs';

import { ApiService } from './api.service';
import { BackendResponse } from './response/backend-response.interface';
import { map } from 'rxjs/operators';
import { Order } from '../admin/orders/order.interface';


@Injectable({
  providedIn: 'root',
})
export class OrderService extends ApiService {

  getOrders(): Observable<Order> {
    if (!this.endpointEnabled('cart')) {
      console.warn(
        'Endpoint "cart" is disabled. To enable change your environment.ts config',
      );
      return EMPTY;
    }
    console.log('Retrieve orders');
    const headersAuth = this.getAuthHeader();
    const url = this.getUrl('cart', '/api/order');
    return this.http.get<BackendResponse<Order>>(url, {
      headers: headersAuth,
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
}
