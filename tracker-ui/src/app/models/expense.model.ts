export type Category = 
  | 'HOUSING' 
  | 'GROCERIES' 
  | 'TRANSPORTATION' 
  | 'ENTERTAINMENT' 
  | 'UTILITIES' 
  | 'MISCELLANEOUS';

export interface Expense {
  id: number;
  title: string;
  amount: number;
  category: Category;
  createdAt: string;
}

export interface CreateExpenseRequest {
  title: string;
  amount: number;
  category: Category;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}