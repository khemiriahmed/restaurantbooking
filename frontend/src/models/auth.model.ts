export type UserRole = 'CLIENT' | 'RESTAURANT' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  role: UserRole;
  firstName: string;
  lastName: string;
}

export interface AuthUser {
  userId: number;
  email: string;
  role: UserRole;
  firstName: string;
  lastName: string;
}
