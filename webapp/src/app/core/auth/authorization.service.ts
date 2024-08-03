import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';
import { Token } from './token.interface';
import { BackendResponse } from '../response/backend-response.interface';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationService extends ApiService {

  authorize(): Observable<any> {
    if (!this.endpointEnabled('auth')) {
      console.warn(
        'Endpoint "auth" is disabled. To enable change your environment.ts config',
      );
      return Observable.create();
    }
    const username = localStorage.getItem('username') || 'default';
    const password = localStorage.getItem('password') || 'default';
    console.log(`user name ${username} and token ${password}`);

    const url = this.getUrl('cart', '/api/auth/login');
    return this.http.post<BackendResponse<Token>>(url, {
      username, password,
    }).pipe(
      map(res => {
        localStorage.setItem('Authorization', `${res.data.token_type} ${res.data.access_token}`);
        return res;
      }));
  }

}
