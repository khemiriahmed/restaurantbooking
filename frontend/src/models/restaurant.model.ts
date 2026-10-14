export interface Restaurant {
  id: number;
  name: string;
  description?: string;
  address: string;
  city: string;
  phone?: string;
  email?: string;
  cuisineType?: string;
  averagePrice?: number;
  imageUrl?: string;
  active: boolean;
}
