import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { AuthorizationService } from './core/auth/authorization.service';
import { CartService } from './cart/cart.service';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnInit {
  constructor(private readonly authService: AuthorizationService,
              private readonly cartService: CartService) {
  }

  ngOnInit(): void {
    this.authService.authorize().pipe(
      map(_ => this.cartService.init())
    ).subscribe();

  }
}
