import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExpenseService } from './services/expense';
import { Category, Expense } from './models/expense.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  private expenseService = inject(ExpenseService);

  // Reactive state management using Angular Signals
  expenses = signal<Expense[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Form model bound via [(ngModel)]
  newExpense = {
    title: '',
    amount: null as number | null,
    category: 'GROCERIES' as Category
  };

  categories: Category[] = [
    'HOUSING',
    'GROCERIES',
    'TRANSPORTATION',
    'ENTERTAINMENT',
    'UTILITIES',
    'MISCELLANEOUS'
  ];

  ngOnInit(): void {
    this.fetchExpenses();
  }

  fetchExpenses(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.expenseService.getExpenses(0, 10).subscribe({
      next: (response) => {
        this.expenses.set(response.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to connect to backend server. Ensure Spring Boot is running on port 8080.');
        this.isLoading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (!this.newExpense.title.trim() || !this.newExpense.amount || this.newExpense.amount <= 0) {
      alert('Please provide a valid title and an amount greater than 0.');
      return;
    }

    const payload = {
      title: this.newExpense.title.trim(),
      amount: this.newExpense.amount,
      category: this.newExpense.category
    };

    this.expenseService.createExpense(payload).subscribe({
      next: (created) => {
        // Prepend newly created item directly to local reactive signal state
        this.expenses.update(prev => [created, ...prev]);

        // Reset form
        this.newExpense.title = '';
        this.newExpense.amount = null;
        this.newExpense.category = 'GROCERIES';
      },
      error: (err) => {
        const errorDetail = err.error?.fieldErrors 
          ? Object.values(err.error.fieldErrors).join('\n') 
          : (err.error?.message || 'Server rejected the request.');
        alert(`Validation Error:\n${errorDetail}`);
      }
    });
  }

  onDelete(id: number): void {
    if (!confirm('Are you sure you want to delete this expense?')) {
      return;
    }

    this.expenseService.deleteExpense(id).subscribe({
      next: () => {
        // Remove item from state without needing a full refetch
        this.expenses.update(prev => prev.filter(e => e.id !== id));
      },
      error: () => {
        alert('Failed to delete expense. The item may have already been removed.');
      }
    });
  }
}