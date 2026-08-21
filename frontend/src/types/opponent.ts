export interface OpponentResponse {
  id: number
  userId: number
  name: string
  memo: string | null
  createdAt: string
  updatedAt: string
}

export interface OpponentCreateRequest {
  name: string
  memo: string | null
}

export type OpponentUpdateRequest = OpponentCreateRequest
