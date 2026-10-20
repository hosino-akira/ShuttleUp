export interface OpponentResponse {
  id: number
  userId: number
  name: string
  enabled: boolean
  available: boolean
  memo: string | null
  createdAt: string
  updatedAt: string
}

export interface OpponentCreateRequest {
  name: string
  memo: string | null
}

export type OpponentUpdateRequest = OpponentCreateRequest & { enabled?: boolean }
