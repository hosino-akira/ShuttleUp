import axios from 'axios'

const analysisClient = axios.create({
  baseURL: import.meta.env.VITE_ANALYSIS_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

export default analysisClient
