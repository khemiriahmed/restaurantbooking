import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse, RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  private readonly tokenKey = 'restaurant_booking_token';
  private readonly userKey = 'restaurant_booking_user';

  /**
   * Connexion
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        this.saveAuthentication(response);
      }),
    );
  }

  /**
   * Inscription
   */
  register(data: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => {
        this.saveAuthentication(response);
      }),
    );
  }

  /**
   * Sauvegarde du token et des informations utilisateur
   */
  private saveAuthentication(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.token);

    localStorage.setItem(
      this.userKey,
      JSON.stringify({
        userId: response.userId,
        email: response.email,
        role: response.role,
      }),
    );
  }

  /**
   * Récupérer le JWT
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Vérifier si l'utilisateur est connecté
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Récupérer l'utilisateur connecté
   */
  getCurrentUser(): {
    userId: number;
    email: string;
    role: string;
  } | null {
    const user = localStorage.getItem(this.userKey);

    if (!user) {
      return null;
    }

    return JSON.parse(user);
  }

  /**
   * Déconnexion
   */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }
}
