import http from './http'
import type { LoginRequest, LoginResponse, PasswordChangeRequest, ProfileUpdateRequest, RegisterRequest, UserResponse } from '../types/auth'

export async function registerUser(request: RegisterRequest): Promise<UserResponse> {
  const response = await http.post<UserResponse>('/auth/register', request)
  return response.data
}

export async function loginUser(request: LoginRequest): Promise<LoginResponse> {
  return (await http.post<LoginResponse>('/auth/login', request)).data
}

export async function getMyProfile(): Promise<UserResponse> {
  return (await http.get<UserResponse>('/auth/me')).data
}

export async function updateMyProfile(request: ProfileUpdateRequest): Promise<UserResponse> {
  return (await http.put<UserResponse>('/auth/me', request)).data
}

export async function changeMyPassword(request: PasswordChangeRequest): Promise<void> {
  await http.put('/auth/me/password', request)
}
