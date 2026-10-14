import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Reservation, ReservationStatus } from '../../../models/reservation.model';
import { ReservationService } from '../../../services/reservation.service';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './reservations.component.html',
  styleUrl: './reservations.component.css',
})
export class ReservationsComponent implements OnInit {
  private readonly reservationService = inject(ReservationService);

  private readonly statusLabels: Record<ReservationStatus, string> = {
    PENDING: 'En attente',
    CONFIRMED: 'Confirmée',
    REJECTED: 'Refusée',
    CANCELLED: 'Annulée',
    COMPLETED: 'Terminée',
  };

  readonly reservations = signal<Reservation[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly cancellingReservationId = signal<number | null>(null);
  readonly statusFilter = signal<ReservationStatus | 'ALL'>('ALL');

  readonly filteredReservations = computed(() => {
    const filter = this.statusFilter();
    const all = this.reservations();

    return filter === 'ALL' ? all : all.filter((r) => r.status === filter);
  });

  readonly upcomingCount = computed(
    () =>
      this.reservations().filter(
        (r) => r.status === 'PENDING' || r.status === 'CONFIRMED',
      ).length,
  );

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.reservationService.getReservations().subscribe({
      next: (data) => {
        this.reservations.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger vos réservations.');
        this.loading.set(false);
      },
    });
  }

  statusLabel(status: ReservationStatus): string {
    return this.statusLabels[status];
  }

  formatTime(time: string | null): string {
    return time ? time.substring(0, 5) : '--:--';
  }

  canCancel(reservation: Reservation): boolean {
    return reservation.status === 'PENDING' || reservation.status === 'CONFIRMED';
  }

  cancelReservation(reservation: Reservation): void {
    if (!confirm('Voulez-vous vraiment annuler cette réservation ?')) {
      return;
    }

    this.successMessage.set('');
    this.errorMessage.set('');
    this.cancellingReservationId.set(reservation.id);

    this.reservationService.cancelReservation(reservation.id).subscribe({
      next: (updatedReservation) => {
        this.reservations.update((list) =>
          list.map((r) => (r.id === updatedReservation.id ? updatedReservation : r)),
        );
        this.cancellingReservationId.set(null);
        this.successMessage.set('Votre réservation a été annulée avec succès.');
      },
      error: (error) => {
        this.cancellingReservationId.set(null);
        this.errorMessage.set(
          error?.error?.message || "Impossible d'annuler la réservation.",
        );
      },
    });
  }
}
