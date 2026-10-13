import { ChangeDetectorRef, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  email = '';
  password = '';

  loading = false;
  errorMessage = '';

  login(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Veuillez remplir tous les champs.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService
      .login({
        email: this.email,
        password: this.password,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.cdr.detectChanges();

          const user = this.authService.getCurrentUser();

          const target =
            user && (user.role === 'ADMIN' || user.role === 'RESTAURANT')
              ? '/dashboard'
              : '/';

          this.router.navigate([target]);
        },

        error: (error) => {
          this.loading = false;
          this.errorMessage =
            error?.status === 401
              ? 'Email ou mot de passe incorrect.'
              : 'Connexion impossible pour le moment.';
          this.cdr.detectChanges();
        },
      });
  }
}
