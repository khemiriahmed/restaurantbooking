import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { Restaurant } from '../../../models/restaurant.model';
import { RestaurantService } from '../../../services/restaurant.service';

@Component({
  selector: 'app-restaurants',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './restaurants.component.html',
  styleUrl: './restaurants.component.css',
})
export class RestaurantsComponent implements OnInit {
  private readonly restaurantService = inject(RestaurantService);

  readonly restaurants = signal<Restaurant[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  readonly search = signal('');
  readonly selectedCity = signal('ALL');
  readonly selectedCuisine = signal('ALL');

  readonly cities = computed(() =>
    [...new Set(this.restaurants().map((r) => r.city).filter(Boolean))].sort(),
  );

  readonly cuisines = computed(() =>
    [...new Set(this.restaurants().map((r) => r.cuisineType).filter(Boolean))].sort() as string[],
  );

  readonly filteredRestaurants = computed(() => {
    const term = this.search().trim().toLowerCase();
    const city = this.selectedCity();
    const cuisine = this.selectedCuisine();

    return this.restaurants().filter((restaurant) => {
      const matchesTerm =
        !term ||
        restaurant.name.toLowerCase().includes(term) ||
        (restaurant.city ?? '').toLowerCase().includes(term) ||
        (restaurant.cuisineType ?? '').toLowerCase().includes(term);

      const matchesCity = city === 'ALL' || restaurant.city === city;
      const matchesCuisine = cuisine === 'ALL' || restaurant.cuisineType === cuisine;

      return matchesTerm && matchesCity && matchesCuisine;
    });
  });

  ngOnInit(): void {
    this.loadRestaurants();
  }

  loadRestaurants(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.restaurantService.getRestaurants().subscribe({
      next: (data) => {
        this.restaurants.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les restaurants.');
        this.loading.set(false);
      },
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.selectedCity.set('ALL');
    this.selectedCuisine.set('ALL');
  }
}
