import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login/LoginPage';
import { SignupPage } from './features/auth/pages/signup/SignupPage';
import { PendingVerification } from './features/auth/pages/pending-verification/pending-verification';
import { VerifyEmailPage } from './features/auth/pages/verify-email/verify-email';
import { DashboardPage } from './features/dashboard/dashboard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { MainLayoutComponent } from './layout/main-layout-component/main-layout-component';
import { AuthLayoutComponent } from './layout/auth-layout-component/auth-layout-component';
import { ForgotPassword } from './features/auth/pages/forgot-password/forgot-password';
import { ResetPassword } from './features/auth/pages/reset-password/reset-password';
import { ChangePassword } from './features/auth/pages/change-password/change-password';

export const routes: Routes = [
  // GUEST ROUTES (Uses Minimal Layout, protected by guestGuard)
  {
    path: '',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      { path: 'login', component: LoginPage },
      { path: 'signup', component: SignupPage },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'forgot-password', component: ForgotPassword },
      { path: 'reset-password', component: ResetPassword },
    ],
  },
  
  // PUBLIC ROUTES (Uses Minimal Layout, no guards)
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      { path: 'pending-verification', component: PendingVerification },
      { path: 'verify-email', component: VerifyEmailPage },
    ]
  },

  // AUTHENTICATED ROUTES (Uses Main Layout with Navbar & Sidebar)
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardPage },
      { path: 'change-password', component: ChangePassword },
    ],
  },
  
  // WILDCARD ROUTE (Must be at the very bottom, outside of all children!)
  { path: '**', redirectTo: '/login' }
];
