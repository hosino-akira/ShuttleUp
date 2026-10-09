import axios from 'axios'
import { getAccessToken, notifyUnauthorized } from '../utils/authSession'

const analysisClient = axios.create({
  baseURL: import.meta.env.VITE_ANALYSIS_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

analysisClient.interceptors.request.use(config => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

analysisClient.interceptors.response.use(response => response, (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401) notifyUnauthorized()
  return Promise.reject(error)
})

export default analysisClient
