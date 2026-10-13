import { ChangeDetectorRef, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  firstName = '';
  lastName = '';
  email = '';
  phone = '';
  password = '';
  confirmPassword = '';

  loading = false;
  errorMessage = '';

  register(): void {
    this.errorMessage = '';

    if (
      !this.firstName ||
      !this.lastName ||
      !this.email ||
      !this.password ||
      !this.confirmPassword
    ) {
      this.errorMessage = 'Veuillez remplir tous les champs.';
      return;
    }

    if (this.password.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;

    this.authService
      .register({
        firstName: this.firstName,
        lastName: this.lastName,
        email: this.email,
        password: this.password,
        phone: this.phone || undefined,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.cdr.detectChanges();
          this.router.navigate(['/']);
        },

        error: (error) => {
          this.loading = false;

          if (error?.status === 409) {
            this.errorMessage = 'Cette adresse email est déjà utilisée.';
          } else if (error?.status === 400) {
            this.errorMessage =
              error?.error?.message || 'Veuillez vérifier les informations saisies.';
          } else {
            this.errorMessage = 'Impossible de créer le compte.';
          }

          this.cdr.detectChanges();
        },
      });
  }
}
