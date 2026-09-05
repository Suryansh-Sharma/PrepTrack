import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';

export const guestGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  // If user is logged in, redirect them away from auth pages
  if (user) {
    if (user.status === 'PENDING_VERIFICATION') {
      return router.createUrlTree(['/pending-verification']);
    }
    return router.createUrlTree(['/dashboard']);
  }

  // User is not logged in - allow access to login/signup
  return true;
};
