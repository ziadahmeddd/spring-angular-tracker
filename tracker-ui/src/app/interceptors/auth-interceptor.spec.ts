import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../services/auth';
import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  function signIn(token: string): void {
    auth.login({ email: 'a@b.com', password: 'Passw0rd!' }).subscribe();
    httpMock.expectOne('http://localhost:8080/api/auth/login')
      .flush({ token, email: 'a@b.com', expiresIn: 3600 });
  }

  it('sends no Authorization header when signed out', () => {
    http.get('http://localhost:8080/api/expenses').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/expenses');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('attaches the bearer token once signed in', () => {
    signIn('jwt-123');

    http.get('http://localhost:8080/api/expenses').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/expenses');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-123');
    req.flush({});
  });

  it('does not attach the token to auth endpoints', () => {
    signIn('jwt-123');

    http.post('http://localhost:8080/api/auth/login', {}).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ token: 'jwt-123', email: 'a@b.com', expiresIn: 3600 });
  });

  it('signs the user out when the API rejects the token', () => {
    signIn('expired-jwt');
    expect(auth.isAuthenticated()).toBe(true);

    http.get('http://localhost:8080/api/expenses').subscribe({ error: () => {} });

    httpMock.expectOne('http://localhost:8080/api/expenses')
      .flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(auth.isAuthenticated()).toBe(false);
  });
});
