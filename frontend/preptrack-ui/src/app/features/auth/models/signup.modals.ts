export interface SignupRequest {
  email: string;
  password: string;
  displayName: string;
  timezone: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  displayName: string;
  plan: string;
  status: string;
  createdAt: string;
}
