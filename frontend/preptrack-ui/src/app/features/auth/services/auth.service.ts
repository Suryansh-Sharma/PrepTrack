import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthenticationInfo, LoginRequest, LoginResponse } from '../models/login.modals';
import { SignupRequest, SignupResponse } from '../models/signup.modals';
import { MeResponseDto } from '../models/auth.common.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/auth';

  readonly currentUser = signal<LoginResponse | null>(null);

  constructor() {
    const savedUser = localStorage.getItem('preptrack_user');
    if (savedUser) {
      this.currentUser.set(JSON.parse(savedUser));
    }
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => {
        this.currentUser.set(response);
        localStorage.setItem('preptrack_user', JSON.stringify(response));
        localStorage.setItem('preptrack_token', response.authentication.accessToken);
      }),
    );
  }

  logout(): void {
    this.currentUser.set(null);
    localStorage.removeItem('preptrack_user');
    localStorage.removeItem('preptrack_token');
  }

  register(request: SignupRequest): Observable<SignupResponse> {
    return this.http.post<SignupResponse>(`${this.apiUrl}/register`, request);
  }

  resendVerification(email: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/resend-verification`,
      { email },
      { responseType: 'text' },
    );
  }

  verifyEmail(token: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/verify-email`, { token }, { responseType: 'text' });
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  resetPassword(payload: { token: string; newPassword: string }): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, payload, { responseType: 'text' });
  }

  refreshToken(refreshToken: string): Observable<AuthenticationInfo> {
    return this.http.post<AuthenticationInfo>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap((newAuthInfo) => {
        // Update local storage token
        localStorage.setItem('preptrack_token', newAuthInfo.accessToken);

        // Update the full user object to keep the new refresh token
        const user = this.currentUser();
        if (user) {
          const updatedUser = { ...user, authentication: newAuthInfo };
          this.currentUser.set(updatedUser);
          localStorage.setItem('preptrack_user', JSON.stringify(updatedUser));
        }
      }),
    );
  }

  getCurrentUser(): Observable<MeResponseDto> {
    return this.http.get<MeResponseDto>(`${this.apiUrl}/me`);
  }
}
