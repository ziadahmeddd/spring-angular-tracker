import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateExpenseRequest, Expense, PagedResponse } from '../models/expense.model';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/expenses';

  getExpenses(page: number = 0, size: number = 10): Observable<PagedResponse<Expense>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<PagedResponse<Expense>>(this.apiUrl, { params });
  }

  createExpense(request: CreateExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, request);
  }

  deleteExpense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}