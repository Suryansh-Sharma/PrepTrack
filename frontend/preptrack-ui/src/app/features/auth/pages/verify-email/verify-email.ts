import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'pt-verify-email',
  imports: [RouterLink, SpinnerComponent],
  templateUrl: './verify-email.html',
})
export class VerifyEmailPage implements OnInit {
  status = signal<'loading' | 'success' | 'error'>('loading');
  errorMessage = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    // Read the token from the query params (?token=...)
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status.set('error');
      this.errorMessage.set('No verification token found in the URL.');
      return;
    }

    // Call the backend to verify the token
    this.authService.verifyEmail(token).subscribe({
      next: (message) => {
        this.status.set('success');
        this.toastService.success('Your email has been verified!');
      },
      error: (err: HttpErrorResponse) => {
        this.status.set('error');

        let backendMessage = 'Verification failed. The link may be expired or invalid.';

        if (typeof err.error === 'string') {
          try {
            const parsedError = JSON.parse(err.error);
            backendMessage = parsedError.message || backendMessage;
          } catch (e) {
            backendMessage = err.error; // It's plain text, not JSON
          }
        } else if (err.error?.message) {
          backendMessage = err.error.message;
        }

        this.errorMessage.set(backendMessage);
      },
    });
  }
}
