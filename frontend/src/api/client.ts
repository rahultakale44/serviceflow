import axios, { AxiosError } from 'axios';
import type { ErrorResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Create axios instance with default config
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response) {
      // Server responded with error
      const errorData = error.response.data;
      console.error('API Error:', errorData);
      
      // You can add global error handling here (e.g., show toast notifications)
      return Promise.reject(errorData);
    } else if (error.request) {
      // Request made but no response
      console.error('Network Error:', error.message);
      return Promise.reject({
        message: 'Network error. Please check your connection.',
        status: 0,
      });
    } else {
      // Something else happened
      console.error('Error:', error.message);
      return Promise.reject({
        message: error.message,
        status: 0,
      });
    }
  }
);

export default apiClient;
