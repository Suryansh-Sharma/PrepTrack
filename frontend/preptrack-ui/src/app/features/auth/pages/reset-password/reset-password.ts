import { Component, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { SpinnerComponent } from '../../../../shared/components/spinner/spinner.component';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'pt-reset-password',
  imports: [ReactiveFormsModule, SpinnerComponent, RouterLink],
  templateUrl: './reset-password.html',
})
export class ResetPassword implements OnInit {
  resetForm;
  isLoading = signal(false);
  apiError = signal<string | null>(null);
  token = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.apiError.set('Invalid or missing reset token.');
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('newPassword')?.value;
    const confirm = control.get('confirmPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  resetPassword(): void {
    if (this.resetForm.invalid || !this.token) {
      this.resetForm.markAllAsTouched();
      if (!this.token && !this.apiError()) {
        this.apiError.set('Cannot reset password without a valid token.');
      }
      return;
    }

    this.isLoading.set(true);
    this.apiError.set(null);

    const newPassword = this.resetForm.value.newPassword!;

    this.authService.resetPassword({ token: this.token, newPassword }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.success('Password successfully reset. You can now log in.');
        this.router.navigate(['/login']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        this.apiError.set(err.error?.message || 'An error occurred while resetting the password.');
      }
    });
  }
}
