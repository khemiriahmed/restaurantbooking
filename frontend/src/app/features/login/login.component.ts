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
        next: (response) => {
          console.log('Connexion réussie :', response);
          this.loading = false;
          this.cdr.detectChanges();
          this.router.navigate(['/']);
        },

        error: (error) => {
          console.error('Erreur de connexion :', error);
          this.loading = false;
          this.errorMessage = 'Email ou mot de passe incorrect.';
          this.cdr.detectChanges();
        },
      });
  }
}
