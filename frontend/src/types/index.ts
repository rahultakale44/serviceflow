// API Response Types
export interface ApiResponse<T> {
  data: T;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Authentication Types
export type UserRole = 'ADMIN' | 'MANAGER' | 'TECHNICIAN' | 'CUSTOMER_SERVICE';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
  lastLoginAt?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

// Customer Types
export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
}

// Vehicle Types
export interface Vehicle {
  id: number;
  registrationNumber: string;
  make: string;
  model: string;
  manufacturingYear?: number;
  fuelType?: string;
  vin?: string;
  customer: CustomerSummary;
  createdAt: string;
  updatedAt: string;
}

export interface VehicleRequest {
  registrationNumber: string;
  make: string;
  model: string;
  manufacturingYear?: number;
  fuelType?: string;
  vin?: string;
  customerId: number;
}

export interface CustomerSummary {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
}

export interface VehicleSummary {
  id: number;
  registrationNumber: string;
  make: string;
  model: string;
  manufacturingYear?: number;
}

// Service Item Types
export interface ServiceItem {
  id: number;
  serviceCode: string;
  name: string;
  description?: string;
  category: string;
  basePrice: number;
  estimatedDurationMinutes: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceItemRequest {
  serviceCode: string;
  name: string;
  description?: string;
  category: string;
  basePrice: number;
  estimatedDurationMinutes: number;
  active?: boolean;
}

// Booking Types
export type BookingStatus = 'REQUESTED' | 'CONFIRMED' | 'CANCELLED' | 'CHECKED_IN';

export interface ServiceBooking {
  id: number;
  bookingNumber: string;
  customer: CustomerSummary;
  vehicle: VehicleSummary;
  bookingDateTime: string;
  customerNotes?: string;
  bookingStatus: BookingStatus;
  estimatedTotalPrice: number;
  estimatedTotalDurationMinutes: number;
  services: BookingServiceItem[];
  createdAt: string;
  updatedAt: string;
}

export interface BookingServiceItem {
  id: number;
  serviceItemId: number;
  serviceCodeSnapshot: string;
  serviceNameSnapshot: string;
  unitPriceSnapshot: number;
  estimatedDurationMinutesSnapshot: number;
  quantity: number;
  lineTotal: number;
}

export interface BookingRequest {
  customerId: number;
  vehicleId: number;
  bookingDateTime: string;
  customerNotes?: string;
  services: {
    serviceId: number;
    quantity: number;
  }[];
}

// Job Types
export type JobStatus = 
  | 'BOOKED'
  | 'CHECKED_IN'
  | 'INSPECTION'
  | 'ESTIMATE_PENDING'
  | 'AWAITING_APPROVAL'
  | 'APPROVED'
  | 'IN_SERVICE'
  | 'QUALITY_CHECK'
  | 'READY_FOR_DELIVERY'
  | 'DELIVERED';

export interface ServiceJob {
  id: number;
  jobCardNumber: string;
  bookingId: number;
  customerId: number;
  customerName: string;
  customerPhone: string;
  vehicleId: number;
  vehicleRegistrationNumber: string;
  vehicleMake: string;
  vehicleModel: string;
  jobStatus: JobStatus;
  complaintDescription?: string;
  inspectionNotes?: string;
  technicianNotes?: string;
  createdAt: string;
  updatedAt: string;
  startedAt?: string;
  completedAt?: string;
  deliveredAt?: string;
  inspections?: JobInspection[];
}

export interface JobInspection {
  id: number;
  jobId: number;
  category: InspectionCategory;
  itemName: string;
  condition: InspectionCondition;
  notes?: string;
  inspectedAt: string;
}

export type InspectionCategory = 
  | 'ENGINE'
  | 'BRAKES'
  | 'TYRES'
  | 'BATTERY'
  | 'ELECTRICAL'
  | 'EXTERIOR'
  | 'INTERIOR'
  | 'GENERAL';

export type InspectionCondition = 'GOOD' | 'NEEDS_ATTENTION' | 'REQUIRES_REPAIR';

// Estimate Types
export type EstimateStatus = 
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'SUPERSEDED';

export type EstimateItemType = 'LABOUR' | 'PART';

export interface RepairEstimate {
  id: number;
  estimateNumber: string;
  jobId: number;
  jobCardNumber: string;
  version: number;
  estimateStatus: EstimateStatus;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  notes?: string;
  items: RepairEstimateItem[];
  createdAt: string;
  updatedAt: string;
}

export interface RepairEstimateItem {
  id: number;
  description: string;
  itemType: EstimateItemType;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface EstimateRequest {
  items: {
    description: string;
    itemType: EstimateItemType;
    quantity: number;
    unitPrice: number;
  }[];
  taxRatePercentage?: number;
  discountAmount?: number;
  notes?: string;
}

// Technician Types
export type TechnicianStatus = 'AVAILABLE' | 'BUSY' | 'ON_LEAVE' | 'INACTIVE';

export type TechnicianSpecialization = 
  | 'ENGINE'
  | 'BRAKES'
  | 'ELECTRICAL'
  | 'BATTERY'
  | 'GENERAL_SERVICE'
  | 'TRANSMISSION'
  | 'SUSPENSION'
  | 'AC_COOLING'
  | 'BODY_PAINT';

export interface Technician {
  id: number;
  technicianCode: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  specialization: TechnicianSpecialization;
  experienceYears: number;
  status: TechnicianStatus;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TechnicianRequest {
  technicianCode: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  specialization: TechnicianSpecialization;
  experienceYears: number;
  status?: TechnicianStatus;
  active?: boolean;
}

// Service Bay Types
export type BayType = 
  | 'GENERAL'
  | 'ENGINE'
  | 'ELECTRICAL'
  | 'WASHING'
  | 'INSPECTION'
  | 'BODY_PAINT';

export interface ServiceBay {
  id: number;
  bayCode: string;
  bayName: string;
  bayType: BayType;
  locationDescription?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceBayRequest {
  bayCode: string;
  bayName: string;
  bayType: BayType;
  locationDescription?: string;
  active?: boolean;
}

// Job Assignment Types
export type AssignmentStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface JobAssignment {
  id: number;
  jobId: number;
  jobCardNumber: string;
  technicianId: number;
  technicianCode: string;
  technicianName: string;
  bayId: number;
  bayCode: string;
  bayName: string;
  scheduledStart: string;
  scheduledEnd: string;
  assignmentStatus: AssignmentStatus;
  notes?: string;
  assignedAt: string;
  updatedAt: string;
  completedAt?: string;
  cancelledAt?: string;
}

export interface JobAssignmentRequest {
  jobId: number;
  technicianId: number;
  bayId: number;
  scheduledStart: string;
  scheduledEnd: string;
  notes?: string;
}

export interface ReassignmentRequest {
  technicianId: number;
  bayId: number;
  scheduledStart: string;
  scheduledEnd: string;
  notes?: string;
}

// Error Response
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
