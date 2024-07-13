import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest,} from '@angular/common/http';
import {Observable} from 'rxjs';
import {NotificationService} from '../notification.service';
import {tap} from 'rxjs/operators';

@Injectable()
export class ErrorPrintInterceptor implements HttpInterceptor {
  constructor(private readonly notificationService: NotificationService) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      tap({
        error: (response: HttpErrorResponse) => {
          const status: number = response.status;
          if ([403, 401].includes(status)) {
            console.log("Should toast because of status " + status);
            this.notificationService.showError(
              `${response.status} ${response.error.message}`,
              6000
            );
            return;
          }
          const url = new URL(request.url);

          this.notificationService.showError(
            `Request to "${url.pathname}" failed. Check the console for the details`,
            0
          );
        },
      })
    );
  }
}
