import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { Restaurant } from '../models/restaurant.model';
import { RestaurantTable } from '../models/restaurant-table.model';
import { RestaurantHour } from '../models/restaurant-hour.model';

@Injectable({
  providedIn: 'root',
})
export class RestaurantService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/restaurants`;

  getRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(this.apiUrl);
  }

  getActiveRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.apiUrl}/active`);
  }

  getMyRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.apiUrl}/mine`);
  }

  getRestaurantById(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.apiUrl}/${id}`);
  }

  getRestaurantsByCity(city: string): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.apiUrl}/city/${encodeURIComponent(city)}`);
  }

  getRestaurantsByCuisine(cuisine: string): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.apiUrl}/cuisine/${encodeURIComponent(cuisine)}`);
  }

  getRestaurantTables(restaurantId: number): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/${restaurantId}/tables`);
  }

  getAvailableTables(restaurantId: number): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/${restaurantId}/tables/available`);
  }

  getRestaurantHours(restaurantId: number): Observable<RestaurantHour[]> {
    return this.http.get<RestaurantHour[]>(`${this.apiUrl}/${restaurantId}/hours`);
  }
}
