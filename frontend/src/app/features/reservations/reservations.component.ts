import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';

import { Reservation } from '../../../models/reservation.model';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './reservations.component.html',
  styleUrl: './reservations.component.css',
})
export class ReservationsComponent implements OnInit {
  private readonly reservationService = inject(ReservationService);

  private readonly authService = inject(AuthService);

  private readonly cdr = inject(ChangeDetectorRef);

  reservations: Reservation[] = [];

  loading = true;
  errorMessage = '';
  successMessage = '';

  cancellingReservationId: number | null = null;

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    const user = this.authService.getCurrentUser();

    if (!user) {
      this.errorMessage = 'Utilisateur non connecté.';

      this.loading = false;
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.reservationService.getReservationsByUser(user.userId).subscribe({
      next: (data) => {
        console.log('Réservations reçues :', data);

        this.reservations = data;
        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur chargement réservations :', error);

        this.errorMessage = 'Impossible de charger vos réservations.';

        this.loading = false;

        this.cdr.detectChanges();
      },
    });
  }

  cancelReservation(reservation: Reservation): void {
    if (!confirm('Voulez-vous vraiment annuler cette réservation ?')) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.cancellingReservationId = reservation.id;

    this.reservationService.cancelReservation(reservation.id).subscribe({
      next: (updatedReservation) => {
        console.log('Réservation annulée :', updatedReservation);

        reservation.status = updatedReservation.status;

        this.cancellingReservationId = null;

        this.successMessage = 'Votre réservation a été annulée avec succès.';

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur annulation réservation :', error);

        this.cancellingReservationId = null;

        this.errorMessage = error.error?.message || 'Impossible d’annuler la réservation.';

        this.cdr.detectChanges();
      },
    });
  }
}
