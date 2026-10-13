import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, RegisterRequest, UserRole } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly tokenKey = 'restaurant_booking_token';
  private readonly userKey = 'restaurant_booking_user';

  private readonly currentUserSignal = signal<AuthUser | null>(this.readStoredUser());

  /** Utilisateur courant, réactif (signal). */
  readonly currentUser = this.currentUserSignal.asReadonly();

  /** Indique si un utilisateur est connecté. */
  readonly isLoggedIn = computed(() => this.currentUserSignal() !== null);

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => this.saveAuthentication(response)),
    );
  }

  register(data: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => this.saveAuthentication(response)),
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.currentUserSignal() !== null && !!this.getToken();
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSignal();
  }

  hasRole(role: UserRole): boolean {
    return this.currentUserSignal()?.role === role;
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isRestaurant(): boolean {
    return this.hasRole('RESTAURANT');
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSignal.set(null);
  }

  private saveAuthentication(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.token);

    const user: AuthUser = {
      userId: response.userId,
      email: response.email,
      role: response.role,
      firstName: response.firstName,
      lastName: response.lastName,
    };

    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private readStoredUser(): AuthUser | null {
    const raw = localStorage.getItem(this.userKey);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      return null;
    }
  }
}
