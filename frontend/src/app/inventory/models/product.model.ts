export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  unit: string;
  category: string;
  imageUrl?: string;
  trend?: number; // e.g., +1.2
}
