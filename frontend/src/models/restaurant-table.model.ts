export interface RestaurantTable {
  id: number;
  tableNumber: number;
  capacity: number;
  available?: boolean;
}