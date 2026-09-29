import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Restaurant } from '../models/restaurant.model';
import { RestaurantTable } from '../models/restaurant-table.model';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {

  private http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/restaurants';

  getRestaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(this.apiUrl);
  }


  getRestaurantById(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.apiUrl}/${id}`);
  }

      getRestaurantTables(
    restaurantId: number
  ): Observable<RestaurantTable[]> {

    return this.http.get<RestaurantTable[]>(
      `${this.apiUrl}/${restaurantId}/tables`
    );
  }
  
}