export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: UserResponse
}

export interface ProfileUpdateRequest {
  name: string
  email: string
}

export interface PasswordChangeRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface UserResponse {
  id: number
  name: string
  email: string
  status: 'ACTIVE' | 'DISABLED'
}
