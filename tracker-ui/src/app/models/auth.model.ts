export interface Credentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  expiresIn: number;
}
