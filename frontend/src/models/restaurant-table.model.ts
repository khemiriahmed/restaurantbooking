export type RestaurantTableStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'MAINTENANCE';

export interface RestaurantTable {
  id: number;
  tableNumber: number;
  capacity: number;
  status: RestaurantTableStatus;
  restaurantId: number;
}
