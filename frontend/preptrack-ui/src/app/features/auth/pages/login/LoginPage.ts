import { Component, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { ToastService } from '../../../../core/services/toast.service';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/login.modals';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'pt-login',
  imports: [ReactiveFormsModule, SpinnerComponent, RouterLink],
  templateUrl: './login-page.html',
})
export class LoginPage {
  loginForm;
  isLoading = signal(false);
  apiError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private toastService: ToastService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.apiError.set(null);

    const request: LoginRequest = {
      email: this.loginForm.value.email ?? '',
      password: this.loginForm.value.password ?? '',
    };

    this.authService.login(request).subscribe({
      next: (response: any) => {
        this.isLoading.set(false);

        if (response.status === 'PENDING_VERIFICATION') {
          this.toastService.show('Please verify your email address to continue.', 'info');
          this.router.navigate(['/pending-verification'], {
            state: { email: response.email },
          });
        } else {
          this.toastService.success(`Welcome back, ${response.displayName}!`);
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);

        const backendMessage =
          err.error?.message || 'An unexpected error occurred. Please try again.';
        this.apiError.set(backendMessage);
      },
    });
  }
}
