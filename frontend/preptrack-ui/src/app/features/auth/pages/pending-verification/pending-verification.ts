import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'pt-pending-verification',
  imports: [ReactiveFormsModule, RouterLink, SpinnerComponent],
  templateUrl: './pending-verification.html',
})
export class PendingVerification {
  resendForm;
  isLoading = signal(false);
  apiError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService,
  ) {
    const navigation = window.history.state;
    const email = navigation?.email || '';

    this.resendForm = this.fb.group({
      email: [email, [Validators.required, Validators.email]],
    });
  }

  resendEmail(): void {
    if (this.resendForm.invalid) {
      this.resendForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.apiError.set(null);

    const email = this.resendForm.value.email ?? '';

    this.authService.resendVerification(email).subscribe({
      next: (message) => {
        this.isLoading.set(false);
        this.toastService.success('Verification email sent! Please check your inbox.');
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);

        let backendMessage = 'Failed to resend email. Please try again.';

        if (typeof err.error === 'string') {
          try {
            const parsedError = JSON.parse(err.error);
            backendMessage = parsedError.message || backendMessage;
          } catch (e) {
            backendMessage = err.error;
          }
        } else if (err.error?.message) {
          backendMessage = err.error.message;
        }

        this.apiError.set(backendMessage);
      },
    });
  }
}
