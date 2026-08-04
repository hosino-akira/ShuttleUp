import axios from 'axios'
import { useGlobalLoading } from '../composables/useGlobalLoading'

const { startLoading, stopLoading } = useGlobalLoading()

const http = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

http.interceptors.request.use(
  (config) => {
    startLoading()
    return config
  },
  (error: unknown) => {
    stopLoading()
    return Promise.reject(error)
  },
)

http.interceptors.response.use(
  (response) => {
    stopLoading()
    return response
  },
  (error: unknown) => {
    stopLoading()
    return Promise.reject(error)
  },
)

export default http
