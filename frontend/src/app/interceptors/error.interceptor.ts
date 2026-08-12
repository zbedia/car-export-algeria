import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      console.error(`[${error.status}] ${error.error?.message || 'Unknown error'}`);
      return throwError(() => error);
    })
  );
};
