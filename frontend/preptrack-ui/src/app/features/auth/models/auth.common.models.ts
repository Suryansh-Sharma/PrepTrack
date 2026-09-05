export interface MeResponseDto {
  id: number;
  email: string;
  displayName: string;
  timezone: string;
  emailVerifiedAt: string | null;
  plan: AppUserPlan;
  status: AppUserStatus;
  deletedAt: string | null;
  failedLoginAttempts: number;
  lockedUntil: string | null;
  createdAt: string;
  updatedAt: string;
}

export type AppUserPlan = 'FREE' | 'PRO';

export type AppUserStatus = 'ACTIVE' | 'LOCKED' | 'DELETED' | 'PENDING_VERIFICATION';
