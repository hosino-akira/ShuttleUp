import axios from 'axios'

/** サーバーが検証した固定メッセージだけを翻訳する。入力された名前は翻訳しない。 */
export function apiError(error: unknown, fallback: string): string {
  if (axios.isAxiosError<{ message?: string }>(error) && [400, 409].includes(error.response?.status ?? 0)) {
    return error.response?.data.message ?? fallback
  }
  return fallback
}
