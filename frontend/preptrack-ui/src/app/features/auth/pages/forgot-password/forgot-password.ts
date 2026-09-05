import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastService } from '../../../../core/services/toast.service';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { AuthService } from '../../services/auth.service';
@Component({
  selector: 'pt-forgot-password',
  imports: [ReactiveFormsModule, SpinnerComponent, RouterLink],
  templateUrl: './forgot-password.html',
})
export class ForgotPassword {
  forgotPasswordForm;
  isLoading = signal(false);
  apiError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private toastService: ToastService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.forgotPasswordForm = fb.nonNullable.group({
      email: ['', [Validators.email, Validators.required]],
    });
  }

  requestForgotPasswordMail(): void {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.apiError.set(null);

    const email = this.forgotPasswordForm.controls.email.value;
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading.set(false);

        this.toastService.success('Password reset link has been sent to your email.');

        this.router.navigate(['/auth/login']);
      },

      error: (error: HttpErrorResponse) => {
        this.isLoading.set(false);

        const message =
          error.error?.message || 'Unable to send password reset email. Please try again.';

        this.apiError.set(message);
        this.toastService.error(message);
      },
    });
  }
}
