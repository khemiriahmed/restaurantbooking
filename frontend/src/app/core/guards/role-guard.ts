import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../../../services/auth.service';
import { UserRole } from '../../../models/auth.model';

export const roleGuard = (roles: UserRole[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.getCurrentUser();

    if (user && roles.includes(user.role)) {
      return true;
    }

    return router.createUrlTree(['/login']);
  };
};
