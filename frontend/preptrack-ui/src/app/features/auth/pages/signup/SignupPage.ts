import { Component, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { Router, RouterLink } from '@angular/router';
import { ToastService } from '../../../../core/services/toast.service';
import { AuthService } from '../../services/auth.service';
import { TIMEZONES } from '../../../../shared/models/commonTypes';
import tr from '@angular/common/locales/tr';
import { SignupRequest } from '../../models/signup.modals';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'pt-signup',
  imports: [ReactiveFormsModule, SpinnerComponent, RouterLink],
  templateUrl: './signup-page.html',
})
export class SignupPage {
  signupForm;
  isLoading = signal(false);
  apiError = signal<string | null>(null);
  timezones = TIMEZONES;

  constructor(
    private fb: FormBuilder,
    private toastService: ToastService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.signupForm = fb.group(
      {
        email: ['', [Validators.email, Validators.required]],
        password: ['', [Validators.minLength(8), Validators.required]],
        confirmPassword: ['', [Validators.required]],
        displayName: ['', [Validators.required]],
        timezone: ['', Validators.required],
      },
      { validators: this.passwordMatchValidator },
    );
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    if (!password || !confirmPassword) return null;
    return password === confirmPassword ? null : { mismatch: true };
  }

  createAccount(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.apiError.set(null);

    const request: SignupRequest = {
      displayName: this.signupForm.value.displayName ?? '',
      email: this.signupForm.value.email ?? '',
      password: this.signupForm.value.password ?? '',
      timezone: this.signupForm.value.timezone ?? '',
    };

    this.authService.register(request).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.toastService.success(
          `Account created for ${response.displayName}! Please check your email to verify your account.`,
        );
        this.router.navigate(['/login']);
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
