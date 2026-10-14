export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface RestaurantHour {
  id: number;
  dayOfWeek: DayOfWeek;
  openingTime: string | null;
  closingTime: string | null;
  closed: boolean;
  restaurantId: number;
}
