import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { AuthService } from '../../../services/auth.service';
import { ReservationService } from '../../../services/reservation.service';
import { RestaurantService } from '../../../services/restaurant.service';
import { Reservation, ReservationStatus } from '../../../models/reservation.model';
import { Restaurant } from '../../../models/restaurant.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly reservationService = inject(ReservationService);
  private readonly restaurantService = inject(RestaurantService);

  readonly currentUser = this.authService.currentUser;
  readonly isAdmin = (): boolean => this.authService.isAdmin();

  readonly restaurants = signal<Restaurant[]>([]);
  readonly reservations = signal<Reservation[]>([]);

  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly processingId = signal<number | null>(null);

  readonly statusFilter = signal<ReservationStatus | 'ALL'>('ALL');

  readonly filteredReservations = computed(() => {
    const filter = this.statusFilter();
    const all = this.reservations();

    return filter === 'ALL' ? all : all.filter((r) => r.status === filter);
  });

  readonly pendingCount = computed(
    () => this.reservations().filter((r) => r.status === 'PENDING').length,
  );
  readonly confirmedCount = computed(
    () => this.reservations().filter((r) => r.status === 'CONFIRMED').length,
  );

  ngOnInit(): void {
    this.loadRestaurants();
    this.loadReservations();
  }

  private loadRestaurants(): void {
    const request$ = this.isAdmin()
      ? this.restaurantService.getRestaurants()
      : this.restaurantService.getMyRestaurants();

    request$.subscribe({
      next: (data) => this.restaurants.set(data),
      error: () => this.restaurants.set([]),
    });
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
        this.errorMessage.set('Impossible de charger les réservations.');
        this.loading.set(false);
      },
    });
  }

  applyStatus(
    reservation: Reservation,
    action: 'confirm' | 'reject' | 'complete' | 'cancel',
  ): void {
    const labels: Record<string, string> = {
      confirm: 'confirmer',
      reject: 'refuser',
      complete: 'marquer comme terminée',
      cancel: 'annuler',
    };

    if (!confirm(`Voulez-vous vraiment ${labels[action]} cette réservation ?`)) {
      return;
    }

    const requests = {
      confirm: () => this.reservationService.confirmReservation(reservation.id),
      reject: () => this.reservationService.rejectReservation(reservation.id),
      complete: () => this.reservationService.completeReservation(reservation.id),
      cancel: () => this.reservationService.cancelReservation(reservation.id),
    };

    this.successMessage.set('');
    this.errorMessage.set('');
    this.processingId.set(reservation.id);

    requests[action]().subscribe({
      next: (updated) => {
        this.reservations.update((list) =>
          list.map((r) => (r.id === updated.id ? updated : r)),
        );
        this.processingId.set(null);
        this.successMessage.set('La réservation a été mise à jour.');
      },
      error: (error) => {
        this.processingId.set(null);
        this.errorMessage.set(
          error?.error?.message || 'Impossible de mettre à jour la réservation.',
        );
      },
    });
  }

  canConfirm(reservation: Reservation): boolean {
    return reservation.status === 'PENDING';
  }

  canComplete(reservation: Reservation): boolean {
    return reservation.status === 'CONFIRMED';
  }

  isFinished(reservation: Reservation): boolean {
    return ['CANCELLED', 'REJECTED', 'COMPLETED'].includes(reservation.status);
  }
}
