export interface LoginRequest {
  email: string;
  password: string;
}
export interface LoginResponse {
  id: number;
  email: string;
  displayName: string;
  timezone: string;
  emailVerifiedAt: string | null;
  plan: string;
  status: string;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  authentication: AuthenticationInfo;
}
export interface AuthenticationInfo {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
  refreshTokenExpiresIn: string;
  issuedAt: string;
  expiresAt: string;
}
