import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink,RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink,RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  isLoggedIn = false;

  userEmail = '';
  userRole = '';

  ngOnInit(): void {
    this.updateAuthenticationState();
  }

  updateAuthenticationState(): void {
    this.isLoggedIn = this.authService.isAuthenticated();

    const user = this.authService.getCurrentUser();

    if (user) {
      this.userEmail = user.email;
      this.userRole = user.role;
    } else {
      this.userEmail = '';
      this.userRole = '';
    }

    this.cdr.detectChanges();
  }

  logout(): void {
    this.authService.logout();

    this.updateAuthenticationState();
  }
}
