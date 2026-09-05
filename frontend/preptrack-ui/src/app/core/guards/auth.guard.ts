import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  // 1. Check if user is logged in
  if (!user) {
    return router.createUrlTree(['/login']);
  }

  // 2. Check if user is verified
  if (user.status === 'PENDING_VERIFICATION') {
    // Redirect to the pending verification page
    router.navigate(['/pending-verification'], {
      state: { email: user.email },
    });
    return false;
  }

  // User is logged in and verified
  return true;
};
