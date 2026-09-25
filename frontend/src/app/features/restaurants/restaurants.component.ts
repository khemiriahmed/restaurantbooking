import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Restaurant } from '../../../models/restaurant.model';
import { RestaurantService } from '../../../services/restaurant.service';

@Component({
  selector: 'app-restaurants',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './restaurants.component.html',
  styleUrl: './restaurants.component.css'
})
export class RestaurantsComponent implements OnInit {

  private restaurantService = inject(RestaurantService);

  restaurants: Restaurant[] = [];

  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadRestaurants();
  }

  loadRestaurants(): void {

    this.loading = true;
    this.errorMessage = '';

    this.restaurantService.getRestaurants().subscribe({

      next: (data) => {
        this.restaurants = data;
        this.loading = false;
      },

      error: (error) => {
        console.error('Erreur lors du chargement des restaurants', error);

        this.errorMessage =
          'Impossible de charger les restaurants.';

        this.loading = false;
      }

    });
  }
}