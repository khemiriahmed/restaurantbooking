import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { ActivatedRoute } from '@angular/router';

import { Restaurant } from '../../../models/restaurant.model';
import { RestaurantService } from '../../../services/restaurant.service';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [],
  templateUrl: './restaurant-detail.component.html',
  styleUrl: './restaurant-detail.component.css'
})
export class RestaurantDetailComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly restaurantService = inject(RestaurantService);
  private readonly cdr = inject(ChangeDetectorRef);

  restaurant: Restaurant | null = null;

  loading = true;
  errorMessage = '';

  ngOnInit(): void {

    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    this.loadRestaurant(id);
  }

  loadRestaurant(id: number): void {

    this.loading = true;
    this.errorMessage = '';

    this.restaurantService
      .getRestaurantById(id)
      .subscribe({

        next: (data) => {
          this.restaurant = data;
          this.loading = false;
          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Erreur lors du chargement du restaurant',
            error
          );

          this.errorMessage =
            'Impossible de charger le restaurant.';

          this.loading = false;

          this.cdr.detectChanges();
        }

      });
  }
}