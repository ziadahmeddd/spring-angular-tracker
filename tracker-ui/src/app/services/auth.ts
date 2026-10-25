import { Service, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, Credentials } from '../models/auth.model';

const TOKEN_KEY = 'tracker.jwt';
const EMAIL_KEY = 'tracker.email';

@Service()
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  private tokenState = signal<string | null>(read(TOKEN_KEY));
  private emailState = signal<string | null>(read(EMAIL_KEY));

  readonly token = this.tokenState.asReadonly();
  readonly email = this.emailState.asReadonly();
  readonly isAuthenticated = computed(() => this.tokenState() !== null);

  register(credentials: Credentials): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, credentials)
      .pipe(tap(response => this.store(response)));
  }

  login(credentials: Credentials): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(tap(response => this.store(response)));
  }

  logout(): void {
    this.tokenState.set(null);
    this.emailState.set(null);
    remove(TOKEN_KEY);
    remove(EMAIL_KEY);
  }

  private store(response: AuthResponse): void {
    this.tokenState.set(response.token);
    this.emailState.set(response.email);
    write(TOKEN_KEY, response.token);
    write(EMAIL_KEY, response.email);
  }
}

// localStorage throws in private-browsing and blocked-storage modes, and is
// unavailable during server-side rendering, so every access is guarded.
function read(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string): void {
  try {
    localStorage.setItem(key, value);
  } catch {
    // Session stays in memory only
  }
}

function remove(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch {
    // Nothing persisted to clear
  }
}
