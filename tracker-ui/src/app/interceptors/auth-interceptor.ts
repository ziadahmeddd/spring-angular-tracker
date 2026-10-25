import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Attaches the JWT to outgoing API calls and clears a session the server has
 * stopped accepting, so an expired token does not leave the UI stuck.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();

  // The auth endpoints issue the token, so they must not carry a stale one
  const isAuthCall = req.url.includes('/api/auth/');

  const request = token && !isAuthCall
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthCall) {
        auth.logout();
      }
      return throwError(() => error);
    })
  );
};
