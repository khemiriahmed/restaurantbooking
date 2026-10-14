export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface ReservationRequest {
  restaurantId: number;
  tableId: number;
  reservationDate: string;
  startTime: string;
  endTime: string;
  numberOfPeople: number;
}

export interface Reservation {
  id: number;
  reservationDate: string;
  startTime: string;
  endTime: string;
  numberOfPeople: number;
  status: ReservationStatus;
  userId: number;
  restaurantId: number;
  restaurantName: string;
  tableId: number;
  tableNumber: number;
  createdAt: string;
}
