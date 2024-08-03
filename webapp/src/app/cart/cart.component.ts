import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { CheckoutService } from './checkout.service';
import { Product, ProductCheckout } from '../products/product.interface';
import { Observable } from 'rxjs';
import { CartService } from './cart.service';
import { map, shareReplay } from 'rxjs/operators';
import { NotificationService } from '../core/notification.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss'],
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: { displayDefaultIndicatorType: false },
    },
  ],
})
export class CartComponent implements OnInit {
  products$!: Observable<ProductCheckout[]>;
  totalPrice$!: Observable<number>;
  totalInCart$!: Observable<number>;
  cartEmpty$!: Observable<boolean>;

  shippingInfo!: UntypedFormGroup;

  constructor(
    private readonly fb: UntypedFormBuilder,
    private readonly checkoutService: CheckoutService,
    private readonly cartService: CartService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
  ) {
  }

  get fullName(): string {
    const { firstName, lastName } = this.shippingInfo.value;
    return `${firstName} ${lastName}`;
  }

  get address(): string {
    return this.shippingInfo.value.address;
  }

  get comment(): string {
    return this.shippingInfo.value.comment;
  }

  ngOnInit(): void {
    this.shippingInfo = this.fb.group({
      lastName: ['', Validators.required],
      firstName: ['', Validators.required],
      address: ['', Validators.required],
      comment: '',
    });

    this.products$ = this.checkoutService.getProductsForCheckout().pipe(
      shareReplay({
        refCount: true,
        bufferSize: 1,
      }),
    );

    this.totalPrice$ = this.products$.pipe(
      map((products) => {
        const total = products.reduce((acc, val) => acc + val.totalPrice, 0);
        return +total.toFixed(2);
      }),
      shareReplay({
        refCount: true,
        bufferSize: 1,
      }),
    );

    this.totalInCart$ = this.cartService.totalInCart$;
    this.cartEmpty$ = this.totalInCart$.pipe(map((count) => count > 0));
  }

  add(product: Product): void {
    this.cartService.addItem(product);
  }

  remove(product: Product): void {
    this.cartService.removeItem(product);
  }

  checkout() {
    this.cartService.checkout({
      delivery: {
        type: `${this.fullName} ${this.address}`,
        comments: this.comment,
      },
    }).subscribe(
      () => {
        this.notificationService.showSuccess('Order has been placed.');
        this.router.navigate(['/'], { replaceUrl: true })
          .then(
            () => this.cartService.init(),
          );
      },
      (error) => {
        this.notificationService.showError('Order has not been placed. Try again');
      },
    );
  }
}
