import { t } from '../i18n';
import axios from 'axios'
import { getAccessToken, getBearerToken, isAccessTokenValid, notifyUnauthorized } from '../utils/authSession'

const analysisClient = axios.create({
  baseURL: import.meta.env.VITE_ANALYSIS_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

analysisClient.interceptors.request.use(config => {
  const token = getAccessToken()
  if (!isAccessTokenValid(token)) {
    notifyUnauthorized(token)
    return Promise.reject(new axios.CanceledError(t('ログイン情報が無効です。'), config))
  }
  config.headers.Authorization = `Bearer ${token}`
  return config
})

analysisClient.interceptors.response.use(response => response, (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401) {
    notifyUnauthorized(getBearerToken(error.config?.headers?.Authorization))
  }
  return Promise.reject(error)
})

export default analysisClient
