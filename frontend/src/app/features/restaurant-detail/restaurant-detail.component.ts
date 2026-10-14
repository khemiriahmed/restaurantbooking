import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Restaurant } from '../../../models/restaurant.model';
import { RestaurantTable } from '../../../models/restaurant-table.model';
import { RestaurantHour } from '../../../models/restaurant-hour.model';
import { RestaurantService } from '../../../services/restaurant.service';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './restaurant-detail.component.html',
  styleUrl: './restaurant-detail.component.css',
})
export class RestaurantDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly restaurantService = inject(RestaurantService);
  private readonly reservationService = inject(ReservationService);
  private readonly authService = inject(AuthService);

  private readonly dayLabels: Record<string, string> = {
    MONDAY: 'Lundi',
    TUESDAY: 'Mardi',
    WEDNESDAY: 'Mercredi',
    THURSDAY: 'Jeudi',
    FRIDAY: 'Vendredi',
    SATURDAY: 'Samedi',
    SUNDAY: 'Dimanche',
  };

  private readonly dayOrder = [
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY',
  ];

  readonly restaurant = signal<Restaurant | null>(null);
  readonly hours = signal<RestaurantHour[]>([]);
  readonly tables = signal<RestaurantTable[]>([]);

  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly tablesLoading = signal(false);
  readonly tablesErrorMessage = signal('');

  readonly selectedTable = signal<RestaurantTable | null>(null);
  readonly reservationDate = signal('');
  readonly startTime = signal('');
  readonly endTime = signal('');
  readonly numberOfPeople = signal(2);

  readonly submitting = signal(false);
  readonly formErrorMessage = signal('');

  readonly isLoggedIn = this.authService.isLoggedIn;

  readonly minDate = new Date().toISOString().split('T')[0];

  readonly eligibleTables = computed(() =>
    this.tables().filter((table) => table.capacity >= this.numberOfPeople()),
  );

  readonly sortedHours = computed(() =>
    [...this.hours()].sort(
      (a, b) => this.dayOrder.indexOf(a.dayOfWeek) - this.dayOrder.indexOf(b.dayOfWeek),
    ),
  );

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.loadRestaurant(id);
    this.loadHours(id);
    this.loadTables(id);
  }

  loadRestaurant(id: number): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.restaurantService.getRestaurantById(id).subscribe({
      next: (data) => {
        this.restaurant.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger le restaurant.');
        this.loading.set(false);
      },
    });
  }

  loadHours(id: number): void {
    this.restaurantService.getRestaurantHours(id).subscribe({
      next: (data) => this.hours.set(data),
      error: () => this.hours.set([]),
    });
  }

  loadTables(id: number): void {
    this.tablesLoading.set(true);
    this.tablesErrorMessage.set('');

    this.restaurantService.getAvailableTables(id).subscribe({
      next: (data) => {
        this.tables.set(data);
        this.tablesLoading.set(false);
      },
      error: () => {
        this.tablesErrorMessage.set('Impossible de charger les tables.');
        this.tablesLoading.set(false);
      },
    });
  }

  selectTable(table: RestaurantTable): void {
    this.selectedTable.set(table);

    if (this.numberOfPeople() > table.capacity) {
      this.numberOfPeople.set(table.capacity);
    }
  }

  dayLabel(day: string): string {
    return this.dayLabels[day] ?? day;
  }

  formatTime(time: string | null): string {
    return time ? time.substring(0, 5) : '--:--';
  }

  submitReservation(): void {
    this.formErrorMessage.set('');

    if (!this.isLoggedIn()) {
      this.formErrorMessage.set('Vous devez être connecté pour réserver.');
      return;
    }

    const restaurant = this.restaurant();
    const table = this.selectedTable();

    if (!restaurant) {
      this.formErrorMessage.set('Restaurant introuvable.');
      return;
    }

    if (!table) {
      this.formErrorMessage.set('Veuillez sélectionner une table.');
      return;
    }

    if (!this.reservationDate()) {
      this.formErrorMessage.set('Veuillez sélectionner une date.');
      return;
    }

    if (!this.startTime() || !this.endTime()) {
      this.formErrorMessage.set('Veuillez renseigner les heures de début et de fin.');
      return;
    }

    if (this.endTime() <= this.startTime()) {
      this.formErrorMessage.set("L'heure de fin doit être après l'heure de début.");
      return;
    }

    if (this.numberOfPeople() < 1) {
      this.formErrorMessage.set('Le nombre de personnes doit être au minimum de 1.');
      return;
    }

    if (this.numberOfPeople() > table.capacity) {
      this.formErrorMessage.set(`La table ${table.tableNumber} accueille au maximum ${table.capacity} personnes.`);
      return;
    }

    this.submitting.set(true);

    this.reservationService
      .createReservation({
        restaurantId: restaurant.id,
        tableId: table.id,
        reservationDate: this.reservationDate(),
        startTime: this.startTime(),
        endTime: this.endTime(),
        numberOfPeople: this.numberOfPeople(),
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.router.navigate(['/reservations']);
        },
        error: (error) => {
          this.submitting.set(false);

          if (error.status === 409) {
            this.formErrorMessage.set(
              error.error?.message || 'Cette réservation entre en conflit avec une réservation existante.',
            );
          } else if (error.status === 400) {
            this.formErrorMessage.set(
              error.error?.message || 'Les données de réservation sont invalides.',
            );
          } else if (error.status === 401) {
            this.formErrorMessage.set('Votre session a expiré. Veuillez vous reconnecter.');
          } else if (error.status === 403) {
            this.formErrorMessage.set("Vous n'avez pas l'autorisation de réserver.");
          } else {
            this.formErrorMessage.set('Impossible de créer la réservation.');
          }
        },
      });
  }
}
