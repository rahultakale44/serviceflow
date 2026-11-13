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

// Flag to prevent redirect loops
let isRedirecting = false;

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response) {
      const status = error.response.status;
      
      // Handle 401 Unauthorized - token expired or invalid
      if (status === 401 && !isRedirecting) {
        isRedirecting = true;
        
        // Clear stored auth data
        localStorage.removeItem('serviceflow_token');
        localStorage.removeItem('serviceflow_user');
        delete apiClient.defaults.headers.common['Authorization'];
        
        // Redirect to login if not already there
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
        
        setTimeout(() => {
          isRedirecting = false;
        }, 1000);
      }
      
      // Handle 403 Forbidden - insufficient permissions
      if (status === 403) {
        console.error('Access denied: Insufficient permissions');
      }
      
      const errorData = error.response.data;
      console.error('API Error:', errorData);
      
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
