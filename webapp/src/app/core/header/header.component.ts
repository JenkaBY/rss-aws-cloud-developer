import { Component, Injector, OnInit } from '@angular/core';
import { CartService } from '../../cart/cart.service';
import { Observable } from 'rxjs';
import { Config } from 'protractor';
import { CONFIG_TOKEN } from '../injection-tokens/config.token';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {
  protected readonly config: Config;
  totalInCart$!: Observable<number>;

  constructor(private readonly cartService: CartService, protected readonly injector: Injector) {
    this.config = injector.get(CONFIG_TOKEN);
  }

  ngOnInit(): void {
    this.totalInCart$ = this.cartService.totalInCart$;
  }
}
