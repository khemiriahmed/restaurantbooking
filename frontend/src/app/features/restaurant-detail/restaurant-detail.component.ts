import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Restaurant } from '../../../models/restaurant.model';
import { RestaurantTable } from '../../../models/restaurant-table.model';
import { RestaurantService } from '../../../services/restaurant.service';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './restaurant-detail.component.html',
  styleUrl: './restaurant-detail.component.css',
})
export class RestaurantDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly restaurantService = inject(RestaurantService);
  private readonly reservationService = inject(ReservationService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  restaurant: Restaurant | null = null;
  loading = true;
  errorMessage = '';

  tables: RestaurantTable[] = [];
  tablesLoading = false;
  tablesErrorMessage = '';

  selectedTable: RestaurantTable | null = null;
  reservationDate = '';
  reservationTime = '';

  numberOfPeople = 1;
  reservationEndTime = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRestaurant(id);
    this.loadTables(id);
  }

  loadRestaurant(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.restaurantService.getRestaurantById(id).subscribe({
      next: (data) => {
        this.restaurant = data;
        this.loading = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement du restaurant', error);

        this.errorMessage = 'Impossible de charger le restaurant.';

        this.loading = false;

        this.cdr.detectChanges();
      },
    });
  }
  loadTables(restaurantId: number): void {
    this.tablesLoading = true;
    this.tablesErrorMessage = '';

    this.restaurantService.getRestaurantTables(restaurantId).subscribe({
      next: (data) => {
        console.log('Tables reçues :', data);

        this.tables = data;
        this.tablesLoading = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement des tables :', error);

        this.tablesErrorMessage = 'Impossible de charger les tables.';

        this.tablesLoading = false;

        this.cdr.detectChanges();
      },
    });
  }
  selectTable(table: RestaurantTable): void {
    this.selectedTable = table;
    console.log('Table sélectionnée :', table);
  }
  continueReservation(): void {
    if (!this.selectedTable) {
      return;
    }

    if (!this.reservationDate) {
      return;
    }

    if (!this.reservationTime) {
      return;
    }

    console.log('Réservation préparée :', {
      restaurantId: this.restaurant?.id,
      tableId: this.selectedTable.id,
      date: this.reservationDate,
      time: this.reservationTime,
    });
  }
  createReservation(): void {
    if (!this.selectedTable) {
      return;
    }

    if (!this.restaurant) {
      return;
    }

    if (!this.reservationDate) {
      return;
    }

    if (!this.reservationTime) {
      return;
    }

    if (!this.reservationEndTime) {
      return;
    }

    if (this.numberOfPeople < 1) {
      return;
    }

    const user = this.authService.getCurrentUser();

    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    const request = {
      userId: user.userId,
      restaurantId: this.restaurant.id,
      tableId: this.selectedTable.id,
      reservationDate: this.reservationDate,
      startTime: this.reservationTime,
      endTime: this.reservationEndTime,
      numberOfPeople: this.numberOfPeople,
    };

    console.log('Réservation envoyée :', request);

    this.reservationService.createReservation(request).subscribe({
      next: (reservation) => {
        console.log('Réservation créée :', reservation);

        this.router.navigate(['/reservations']);
      },

      error: (error) => {
        console.error('Erreur création réservation :', error);

        this.errorMessage = 'Impossible de créer la réservation.';

        this.cdr.detectChanges();
      },
    });
  }
}
