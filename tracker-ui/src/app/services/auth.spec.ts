import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('starts unauthenticated with no stored token', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.token()).toBeNull();
  });

  it('stores the token returned by login', () => {
    service.login({ email: 'a@b.com', password: 'Passw0rd!' }).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'jwt-123', email: 'a@b.com', expiresIn: 3600 });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.token()).toBe('jwt-123');
    expect(service.email()).toBe('a@b.com');
  });

  it('stores the token returned by register', () => {
    service.register({ email: 'new@b.com', password: 'Passw0rd!' }).subscribe();

    httpMock.expectOne('http://localhost:8080/api/auth/register')
      .flush({ token: 'jwt-456', email: 'new@b.com', expiresIn: 3600 });

    expect(service.token()).toBe('jwt-456');
  });

  it('clears the session on logout', () => {
    service.login({ email: 'a@b.com', password: 'Passw0rd!' }).subscribe();
    httpMock.expectOne('http://localhost:8080/api/auth/login')
      .flush({ token: 'jwt-123', email: 'a@b.com', expiresIn: 3600 });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('tracker.jwt')).toBeNull();
  });

  it('restores a token persisted by an earlier session', () => {
    localStorage.setItem('tracker.jwt', 'jwt-from-storage');
    localStorage.setItem('tracker.email', 'a@b.com');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    const restored = TestBed.inject(AuthService);
    expect(restored.isAuthenticated()).toBe(true);
    expect(restored.token()).toBe('jwt-from-storage');

    httpMock = TestBed.inject(HttpTestingController);
  });
});
