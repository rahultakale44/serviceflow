import apiClient from './client';
import type {
  Customer,
  CustomerRequest,
  Vehicle,
  VehicleRequest,
  ServiceItem,
  ServiceItemRequest,
  ServiceBooking,
  BookingRequest,
  ServiceJob,
  RepairEstimate,
  EstimateRequest,
  Technician,
  TechnicianRequest,
  ServiceBay,
  ServiceBayRequest,
  JobAssignment,
  JobAssignmentRequest,
  ReassignmentRequest,
  PagedResponse,
  JobInspection,
  InspectionCategory,
  InspectionCondition,
  JobStatus,
  BookingStatus,
} from '../types';

// Pagination params
export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: 'ASC' | 'DESC';
}

// Customer API
export const customerApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<Customer>>('/customers', { params }),
  
  getById: (id: number) =>
    apiClient.get<Customer>(`/customers/${id}`),
  
  create: (data: CustomerRequest) =>
    apiClient.post<Customer>('/customers', data),
  
  update: (id: number, data: CustomerRequest) =>
    apiClient.put<Customer>(`/customers/${id}`, data),
  
  delete: (id: number) =>
    apiClient.delete(`/customers/${id}`),
};

// Vehicle API
export const vehicleApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<Vehicle>>('/vehicles', { params }),
  
  getById: (id: number) =>
    apiClient.get<Vehicle>(`/vehicles/${id}`),
  
  getByCustomer: (customerId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<Vehicle>>(`/vehicles/customer/${customerId}`, { params }),
  
  create: (data: VehicleRequest) =>
    apiClient.post<Vehicle>('/vehicles', data),
  
  update: (id: number, data: VehicleRequest) =>
    apiClient.put<Vehicle>(`/vehicles/${id}`, data),
  
  delete: (id: number) =>
    apiClient.delete(`/vehicles/${id}`),
};

// Service Item API
export const serviceApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceItem>>('/services', { params }),
  
  getActive: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceItem>>('/services/active', { params }),
  
  getById: (id: number) =>
    apiClient.get<ServiceItem>(`/services/${id}`),
  
  getByCategory: (category: string, params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceItem>>(`/services/category/${category}`, { params }),
  
  create: (data: ServiceItemRequest) =>
    apiClient.post<ServiceItem>('/services', data),
  
  update: (id: number, data: ServiceItemRequest) =>
    apiClient.put<ServiceItem>(`/services/${id}`, data),
  
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<ServiceItem>(`/services/${id}/status`, { active }),
  
  delete: (id: number) =>
    apiClient.delete(`/services/${id}`),
};

// Booking API
export const bookingApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceBooking>>('/bookings', { params }),
  
  getById: (id: number) =>
    apiClient.get<ServiceBooking>(`/bookings/${id}`),
  
  getByCustomer: (customerId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceBooking>>(`/bookings/customer/${customerId}`, { params }),
  
  getByVehicle: (vehicleId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceBooking>>(`/bookings/vehicle/${vehicleId}`, { params }),
  
  create: (data: BookingRequest) =>
    apiClient.post<ServiceBooking>('/bookings', data),
  
  updateStatus: (id: number, status: BookingStatus) =>
    apiClient.patch<ServiceBooking>(`/bookings/${id}/status`, { bookingStatus: status }),
  
  cancel: (id: number) =>
    apiClient.delete(`/bookings/${id}`),
};

// Job API
export const jobApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceJob>>('/jobs', { params }),
  
  getById: (id: number) =>
    apiClient.get<ServiceJob>(`/jobs/${id}`),
  
  getByJobCardNumber: (jobCardNumber: string) =>
    apiClient.get<ServiceJob>(`/jobs/job-card/${jobCardNumber}`),
  
  getByBooking: (bookingId: number) =>
    apiClient.get<ServiceJob>(`/jobs/booking/${bookingId}`),
  
  getByVehicle: (vehicleId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<ServiceJob>>(`/jobs/vehicle/${vehicleId}`, { params }),
  
  create: (data: { bookingId: number; complaintDescription?: string; inspectionNotes?: string }) =>
    apiClient.post<ServiceJob>('/jobs', data),
  
  updateStatus: (id: number, jobStatus: JobStatus, notes?: string) =>
    apiClient.patch<ServiceJob>(`/jobs/${id}/status`, { jobStatus, notes }),
  
  updateNotes: (id: number, notes: { inspectionNotes?: string; technicianNotes?: string }) =>
    apiClient.patch<ServiceJob>(`/jobs/${id}/notes`, notes),
  
  addInspection: (
    id: number,
    data: { category: InspectionCategory; itemName: string; condition: InspectionCondition; notes?: string }
  ) =>
    apiClient.post<JobInspection>(`/jobs/${id}/inspections`, data),
  
  getInspections: (id: number) =>
    apiClient.get<JobInspection[]>(`/jobs/${id}/inspections`),
};

// Estimate API
export const estimateApi = {
  getByJob: (jobId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<RepairEstimate>>(`/jobs/${jobId}/estimates`, { params }),
  
  getCurrent: (jobId: number) =>
    apiClient.get<RepairEstimate>(`/jobs/${jobId}/estimates/current`),
  
  getById: (jobId: number, estimateId: number) =>
    apiClient.get<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}`),
  
  create: (jobId: number, data: EstimateRequest) =>
    apiClient.post<RepairEstimate>(`/jobs/${jobId}/estimates`, data),
  
  update: (jobId: number, estimateId: number, data: EstimateRequest) =>
    apiClient.put<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}`, data),
  
  submit: (jobId: number, estimateId: number) =>
    apiClient.post<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}/submit`),
  
  approve: (jobId: number, estimateId: number) =>
    apiClient.post<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}/approve`),
  
  reject: (jobId: number, estimateId: number, rejectionReason: string, comments?: string) =>
    apiClient.post<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}/reject`, {
      rejectionReason,
      comments,
    }),
  
  revise: (jobId: number, estimateId: number) =>
    apiClient.post<RepairEstimate>(`/jobs/${jobId}/estimates/${estimateId}/revise`),
};

// Technician API
export const technicianApi = {
  getAll: (params?: PaginationParams & { status?: string; specialization?: string; activeOnly?: boolean }) =>
    apiClient.get<PagedResponse<Technician>>('/technicians', { params }),
  
  getById: (id: number) =>
    apiClient.get<Technician>(`/technicians/${id}`),
  
  create: (data: TechnicianRequest) =>
    apiClient.post<Technician>('/technicians', data),
  
  update: (id: number, data: TechnicianRequest) =>
    apiClient.put<Technician>(`/technicians/${id}`, data),
  
  updateStatus: (id: number, status: string, active?: boolean) =>
    apiClient.patch<Technician>(`/technicians/${id}/status`, { status, active }),
};

// Service Bay API
export const serviceBayApi = {
  getAll: (params?: PaginationParams & { bayType?: string; activeOnly?: boolean }) =>
    apiClient.get<PagedResponse<ServiceBay>>('/service-bays', { params }),
  
  getById: (id: number) =>
    apiClient.get<ServiceBay>(`/service-bays/${id}`),
  
  create: (data: ServiceBayRequest) =>
    apiClient.post<ServiceBay>('/service-bays', data),
  
  update: (id: number, data: ServiceBayRequest) =>
    apiClient.put<ServiceBay>(`/service-bays/${id}`, data),
  
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<ServiceBay>(`/service-bays/${id}/status`, { active }),
};

// Job Assignment API
export const assignmentApi = {
  getAll: (params?: PaginationParams) =>
    apiClient.get<PagedResponse<JobAssignment>>('/job-assignments', { params }),
  
  getById: (id: number) =>
    apiClient.get<JobAssignment>(`/job-assignments/${id}`),
  
  getByJob: (jobId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<JobAssignment>>(`/job-assignments/job/${jobId}`, { params }),
  
  getByTechnician: (technicianId: number, params?: PaginationParams) =>
    apiClient.get<PagedResponse<JobAssignment>>(`/job-assignments/technician/${technicianId}`, { params }),
  
  create: (data: JobAssignmentRequest) =>
    apiClient.post<JobAssignment>('/job-assignments', data),
  
  complete: (id: number) =>
    apiClient.post<JobAssignment>(`/job-assignments/${id}/complete`),
  
  cancel: (id: number) =>
    apiClient.post<JobAssignment>(`/job-assignments/${id}/cancel`),
  
  reassign: (id: number, data: ReassignmentRequest) =>
    apiClient.post<JobAssignment>(`/job-assignments/${id}/reassign`, data),
};
