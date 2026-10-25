import { Component, computed, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ExpenseService } from './services/expense';
import { AuthService } from './services/auth';
import { Category, Expense } from './models/expense.model';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-root',
  imports: [DatePipe, FormsModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  private expenseService = inject(ExpenseService);
  private formBuilder = inject(FormBuilder);
  protected auth = inject(AuthService);

  // Reactive state management using Angular Signals
  expenses = signal<Expense[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  authMode = signal<AuthMode>('login');
  authError = signal<string | null>(null);
  isSubmittingAuth = signal<boolean>(false);

  readonly authHeading = computed(() =>
    this.authMode() === 'login' ? 'Sign In' : 'Create Account'
  );

  // Reactive form: validation is declared once, not re-checked by hand on submit
  authForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

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

  constructor() {
    // Load on sign-in, clear on sign-out. Replaces ngOnInit, which would have
    // fetched before a token existed and taken a guaranteed 401.
    effect(() => {
      if (this.auth.isAuthenticated()) {
        this.fetchExpenses();
      } else {
        this.expenses.set([]);
        this.errorMessage.set(null);
      }
    });
  }

  switchMode(mode: AuthMode): void {
    this.authMode.set(mode);
    this.authError.set(null);
    this.authForm.reset();
  }

  onAuthSubmit(): void {
    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      return;
    }

    const credentials = this.authForm.getRawValue();
    const request = this.authMode() === 'login'
      ? this.auth.login(credentials)
      : this.auth.register(credentials);

    this.isSubmittingAuth.set(true);
    this.authError.set(null);

    request.subscribe({
      next: () => {
        this.isSubmittingAuth.set(false);
        this.authForm.reset();
      },
      error: (err) => {
        this.isSubmittingAuth.set(false);
        this.authError.set(readServerMessage(err, 'Authentication failed.'));
      }
    });
  }

  onLogout(): void {
    this.auth.logout();
  }

  fetchExpenses(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.expenseService.getExpenses(0, 10).subscribe({
      next: (response) => {
        this.expenses.set(response.content);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        // A 401 is already handled by the interceptor, which signs the user out
        if (err.status !== 401) {
          this.errorMessage.set('Failed to connect to backend server. Ensure Spring Boot is running on port 8080.');
        }
      }
    });
  }

  onSubmit(): void {
    if (!this.newExpense.title.trim() || !this.newExpense.amount || this.newExpense.amount <= 0) {
      this.errorMessage.set('Please provide a valid title and an amount greater than 0.');
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
        this.errorMessage.set(null);

        // Reset form
        this.newExpense.title = '';
        this.newExpense.amount = null;
        this.newExpense.category = 'GROCERIES';
      },
      error: (err) => {
        this.errorMessage.set(readServerMessage(err, 'Server rejected the request.'));
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
        this.errorMessage.set('Failed to delete expense. The item may have already been removed.');
      }
    });
  }
}

/** Unpacks the shapes GlobalExceptionHandler returns into one display string. */
function readServerMessage(err: unknown, fallback: string): string {
  const body = (err as { error?: { fieldErrors?: Record<string, string>; message?: string } }).error;

  if (body?.fieldErrors) {
    return Object.values(body.fieldErrors).join(' ');
  }

  return body?.message ?? fallback;
}
