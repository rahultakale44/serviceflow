# ServiceFlow

A comprehensive vehicle service center management platform built with Spring Boot.

## Project Overview

ServiceFlow is a modern backend system for managing vehicle service operations, including customer management, vehicle registration, service scheduling, and billing. Built with industry-standard technologies and best practices.

**Repository**: https://github.com/rahultakale44/serviceflow  
**Branch**: main  
**Status**: Phase 7 Complete ✅

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.1.1
- **Build Tool**: Maven
- **Database**: MySQL 9.7
- **Security**: Spring Security
- **Validation**: Bean Validation
- **ORM**: Spring Data JPA / Hibernate
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Test Database**: H2
- **Utilities**: Lombok

## Development Progress

### ✅ Phase 1: Customer Management (Completed)

Complete CRUD operations for customer management with comprehensive validation and error handling.

**Features:**
- Customer entity with validation
- Repository layer with custom queries
- Service layer with business logic
- REST API endpoints under `/api/v1/customers`
- Comprehensive exception handling
- Full test coverage (23 tests)

**Endpoints:**
- `POST /api/v1/customers` - Create customer
- `GET /api/v1/customers/{id}` - Get customer by ID
- `GET /api/v1/customers` - Get all customers (paginated)
- `PUT /api/v1/customers/{id}` - Update customer
- `DELETE /api/v1/customers/{id}` - Delete customer

**Commits:**
- `364363a` - feat(customer): add customer entity and repository
- `7d646dd` - feat(customer): implement DTOs, service layer and exception handling
- `a29d0ed` - feat(customer): add REST endpoints and security configuration
- `c5f2c6c` - test(customer): add comprehensive unit tests for customer management

### ✅ Phase 2: Vehicle Management (Completed)

Complete vehicle management system with proper customer relationships and business rules.

**Features:**
- Vehicle entity with ManyToOne relationship to Customer
- Unique constraints on registration number and VIN
- Registration number and VIN normalization (uppercase)
- Support for optional VIN field
- Prevents customer deletion when vehicles exist
- Prevents vehicle ownership changes during updates
- Comprehensive validation and error handling
- Full test coverage (34 tests)

**Entity Structure:**
```
Vehicle
├── id (Long, Primary Key)
├── registrationNumber (String, Unique, Mandatory)
├── make (String, Mandatory)
├── model (String, Mandatory)
├── manufacturingYear (Integer)
├── fuelType (String)
├── vin (String, Unique when provided, Optional)
├── customer (ManyToOne → Customer)
├── createdAt (LocalDateTime)
└── updatedAt (LocalDateTime)
```

**Business Rules:**
- Registration numbers are normalized to uppercase before storage
- VIN values are normalized to uppercase when provided
- Duplicate registration numbers are rejected with 409 Conflict
- Duplicate VINs are rejected with 409 Conflict
- Vehicles must belong to an existing customer (404 if customer not found)
- Vehicle ownership cannot be changed via update operations
- Customers with existing vehicles cannot be deleted (409 Conflict)

**Endpoints:**
- `POST /api/v1/vehicles` - Create vehicle (201 Created)
- `GET /api/v1/vehicles/{id}` - Get vehicle by ID (200 OK)
- `GET /api/v1/vehicles` - Get all vehicles with pagination (200 OK)
- `GET /api/v1/vehicles/customer/{customerId}` - Get customer's vehicles (200 OK)
- `PUT /api/v1/vehicles/{id}` - Update vehicle (200 OK)
- `DELETE /api/v1/vehicles/{id}` - Delete vehicle (204 No Content)

**Commits:**
- `b16ec39` - feat(vehicle): add vehicle entity and repository
- `6f3e33d` - feat(vehicle): implement vehicle service and business rules
- `cb7344f` - feat(vehicle): add vehicle REST endpoints and security config
- `5fda144` - test(vehicle): add comprehensive test coverage for vehicle management

### ✅ Phase 3: Service Catalogue Management (Completed)

Complete service catalogue system for managing offered services with pricing and duration.

**Features:**
- ServiceItem entity with code normalization
- Soft delete support (deactivation)
- Active/inactive service filtering
- Category-based service organization
- BigDecimal for precise price handling
- Comprehensive validation and error handling
- Full test coverage (32 tests)

**Entity Structure:**
```
ServiceItem
├── id (Long, Primary Key)
├── serviceCode (String, Unique, Mandatory, Normalized to uppercase)
├── name (String, Mandatory)
├── description (String, Optional)
├── category (String, Mandatory)
├── basePrice (BigDecimal, Mandatory, Non-negative)
├── estimatedDurationMinutes (Integer, Mandatory, Positive)
├── active (Boolean, Default true)
├── createdAt (LocalDateTime)
└── updatedAt (LocalDateTime)
```

**Business Rules:**
- Service codes are normalized to uppercase and trimmed
- Duplicate service codes are rejected with 409 Conflict
- Base price must be non-negative (zero or positive)
- Estimated duration must be at least 1 minute
- DELETE performs soft delete (sets active=false) to preserve history
- Inactive services remain retrievable but not offered for new bookings
- Active status is preserved during updates unless explicitly changed

**Endpoints:**
- `POST /api/v1/services` - Create service item (201 Created)
- `GET /api/v1/services/{id}` - Get service by ID (200 OK)
- `GET /api/v1/services` - Get all services with pagination (200 OK)
- `GET /api/v1/services/active` - Get active services with pagination (200 OK)
- `GET /api/v1/services/category/{category}` - Get services by category (200 OK)
- `PUT /api/v1/services/{id}` - Update service (200 OK)
- `PATCH /api/v1/services/{id}/status` - Activate/deactivate service (200 OK)
- `DELETE /api/v1/services/{id}` - Soft delete (deactivate) service (204 No Content)

**Example Service Catalogue Entries:**
- General Vehicle Service (category: Maintenance)
- Engine Oil Change (category: Maintenance)
- Brake Inspection (category: Safety)
- Brake Pad Replacement (category: Repair)
- Tyre Rotation (category: Maintenance)
- Wheel Alignment (category: Maintenance)
- Battery Replacement (category: Electrical)
- Air Filter Replacement (category: Maintenance)
- AC Inspection (category: Comfort)
- Vehicle Washing (category: Cleaning)

**Commits:**
- `f683351` - feat(catalogue): add service item entity and repository
- `8e676d6` - test(catalogue): add comprehensive service catalogue tests

### ✅ Phase 4: Service Booking Management (Completed)

Complete booking system connecting customers, vehicles, and services with historical price preservation.

**Features:**
- ServiceBooking entity with line item snapshots
- BookingServiceItem entity preserving historical pricing
- Booking status lifecycle management
- Price and duration snapshot preservation
- Multi-service booking support
- Duplicate service consolidation
- Atomic booking creation with rollback
- Customer and vehicle booking history
- Soft cancellation preserving records
- Full test coverage (33 tests)

**Entity Relationships:**
```
ServiceBooking
├── id (Long, Primary Key)
├── bookingNumber (String, Unique, Auto-generated)
├── customer (ManyToOne → Customer)
├── vehicle (ManyToOne → Vehicle)
├── bookingDateTime (LocalDateTime, Future)
├── customerNotes (String, Optional)
├── bookingStatus (Enum: REQUESTED, CONFIRMED, CANCELLED, CHECKED_IN)
├── estimatedTotalPrice (BigDecimal)
├── estimatedTotalDurationMinutes (Integer)
├── bookingServiceItems (OneToMany → BookingServiceItem)
├── createdAt (LocalDateTime)
└── updatedAt (LocalDateTime)

BookingServiceItem (Line Items with Historical Snapshots)
├── id (Long, Primary Key)
├── booking (ManyToOne → ServiceBooking)
├── serviceItem (ManyToOne → ServiceItem)
├── serviceCodeSnapshot (String, Preserved from catalogue)
├── serviceNameSnapshot (String, Preserved from catalogue)
├── unitPriceSnapshot (BigDecimal, Preserved from catalogue)
├── estimatedDurationMinutesSnapshot (Integer, Preserved from catalogue)
├── quantity (Integer, Positive)
└── lineTotal (BigDecimal, Calculated)
```

**Booking Status Transitions:**
```
REQUESTED → CONFIRMED     (Booking confirmed)
REQUESTED → CANCELLED     (Booking cancelled before confirmation)
CONFIRMED → CANCELLED     (Booking cancelled after confirmation)
CONFIRMED → CHECKED_IN    (Customer checked in)

Terminal States: CANCELLED, CHECKED_IN
```

**Business Rules:**
1. Customer must exist (404 if not found)
2. Vehicle must exist (404 if not found)
3. Vehicle must belong to the specified customer
4. At least one service must be selected
5. All selected services must exist and be active
6. Booking date/time must be in the future
7. Quantity must be positive (≥ 1)
8. Duplicate service selections are consolidated
9. Prices and durations read from database (client cannot specify)
10. Historical snapshots preserved even if catalogue changes
11. Status transitions validated (invalid transitions rejected with 400)
12. DELETE performs soft cancellation (sets status=CANCELLED)
13. Atomic creation (all-or-nothing transaction)
14. Unique booking numbers auto-generated (format: BK-YYYYMMDD-XXXXX)

**Endpoints:**
- `POST /api/v1/bookings` - Create booking (201 Created)
- `GET /api/v1/bookings/{id}` - Get booking by ID (200 OK)
- `GET /api/v1/bookings` - Get all bookings with pagination (200 OK)
- `GET /api/v1/bookings/customer/{customerId}` - Get customer bookings (200 OK)
- `GET /api/v1/bookings/vehicle/{vehicleId}` - Get vehicle bookings (200 OK)
- `PATCH /api/v1/bookings/{id}/status` - Update booking status (200 OK)
- `DELETE /api/v1/bookings/{id}` - Cancel booking (204 No Content)

**Example Booking Request:**
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "bookingDateTime": "2026-11-15T10:00:00",
  "customerNotes": "Regular maintenance service",
  "services": [
    {"serviceId": 1, "quantity": 1},
    {"serviceId": 2, "quantity": 2}
  ]
}
```

**Example Booking Response:**
```json
{
  "id": 1,
  "bookingNumber": "BK-20261110-00001",
  "customer": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "email": "john@example.com"
  },
  "vehicle": {
    "id": 1,
    "registrationNumber": "ABC123",
    "make": "Toyota",
    "model": "Camry",
    "manufacturingYear": 2022
  },
  "bookingDateTime": "2026-11-15T10:00:00",
  "customerNotes": "Regular maintenance service",
  "bookingStatus": "REQUESTED",
  "estimatedTotalPrice": 150.00,
  "estimatedTotalDurationMinutes": 120,
  "services": [
    {
      "id": 1,
      "serviceItemId": 1,
      "serviceCode": "OC001",
      "serviceName": "Oil Change",
      "unitPrice": 50.00,
      "estimatedDurationMinutes": 30,
      "quantity": 1,
      "lineTotal": 50.00
    },
    {
      "id": 2,
      "serviceItemId": 2,
      "serviceCode": "BI001",
      "serviceName": "Brake Inspection",
      "unitPrice": 50.00,
      "estimatedDurationMinutes": 45,
      "quantity": 2,
      "lineTotal": 100.00
    }
  ],
  "createdAt": "2026-11-10T22:30:00",
  "updatedAt": "2026-11-10T22:30:00"
}
```

**Commits:**
- `94fca08` - feat(booking): add booking entities and line item snapshots
- `d073b6f` - feat(booking): implement booking repository and business rules
- `0df8acd` - feat(booking): add booking creation and retrieval APIs
- `8733105` - test(booking): add comprehensive booking test coverage

### ✅ Phase 5: Service Job/Job Card Management (Completed)

Central workshop management system converting checked-in bookings into trackable job cards with complete lifecycle management.

**Features:**
- ServiceJob entity with OneToOne booking relationship
- 10-state job status lifecycle management
- Vehicle inspection system with category tracking
- Unique job card number generation (JC-YYYYMMDD-00001)
- Quality check rework flow support
- Timestamp tracking (started, completed, delivered)
- Job notes management (inspection + technician)
- Vehicle service history tracking
- Comprehensive validation and error handling
- Full test coverage (46 tests)

**Entity Relationships:**
```
ServiceJob
├── id (Long, Primary Key)
├── jobCardNumber (String, Unique, Auto-generated: JC-YYYYMMDD-00001)
├── booking (OneToOne → ServiceBooking)
├── customer (ManyToOne → Customer)
├── vehicle (ManyToOne → Vehicle)
├── jobStatus (Enum, 10 states)
├── complaintDescription (String, Optional)
├── inspectionNotes (String, Optional)
├── technicianNotes (String, Optional)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
├── startedAt (LocalDateTime, Set when IN_SERVICE)
├── completedAt (LocalDateTime, Set when QUALITY_CHECK)
├── deliveredAt (LocalDateTime, Set when DELIVERED)
└── inspections (OneToMany → JobInspection)

JobInspection (Vehicle Inspection Records)
├── id (Long, Primary Key)
├── job (ManyToOne → ServiceJob)
├── inspectionCategory (Enum: ENGINE, BRAKES, TYRES, etc.)
├── itemName (String, Specific inspection item)
├── condition (Enum: GOOD, NEEDS_ATTENTION, REQUIRES_REPAIR)
├── notes (String, Optional)
└── inspectedAt (LocalDateTime)
```

**Job Status Lifecycle (10 States):**
```
BOOKED → CHECKED_IN → INSPECTION → ESTIMATE_PENDING → AWAITING_APPROVAL 
       → APPROVED → IN_SERVICE → QUALITY_CHECK → READY_FOR_DELIVERY → DELIVERED

Rework Path: QUALITY_CHECK → IN_SERVICE (when quality check fails)
```

**Inspection Categories:**
- ENGINE - Engine and powertrain components
- BRAKES - Brake system and components
- TYRES - Tyre condition and pressure
- BATTERY - Battery health and connections
- ELECTRICAL - Electrical systems
- EXTERIOR - Body and paint condition
- INTERIOR - Interior condition and functionality
- GENERAL - General observations

**Inspection Conditions:**
- GOOD - Component in good condition
- NEEDS_ATTENTION - Requires monitoring
- REQUIRES_REPAIR - Needs immediate repair

**Business Rules:**
1. Job card can only be created from bookings in CHECKED_IN status
2. One booking can have only one job card (OneToOne relationship)
3. Job card numbers are unique and auto-generated
4. Status transitions are validated (invalid transitions rejected with 400)
5. Quality check can return to in-service for rework
6. Customer and vehicle must exist when creating job
7. Inspection categories and conditions are predefined enums
8. Multiple inspection records allowed per job
9. Timestamps automatically updated based on status changes
10. Vehicle service history accessible via job queries

**Endpoints:**
- `POST /api/v1/jobs` - Create job from booking (201 Created)
- `GET /api/v1/jobs/{id}` - Get job by ID (200 OK)
- `GET /api/v1/jobs/job-card/{jobCardNumber}` - Get job by job card number (200 OK)
- `GET /api/v1/jobs/booking/{bookingId}` - Get job by booking ID (200 OK)
- `GET /api/v1/jobs` - Get all jobs with pagination (200 OK)
- `GET /api/v1/jobs/vehicle/{vehicleId}` - Get vehicle service history (200 OK)
- `PATCH /api/v1/jobs/{id}/status` - Update job status (200 OK)
- `PATCH /api/v1/jobs/{id}/notes` - Update job notes (200 OK)
- `POST /api/v1/jobs/{id}/inspections` - Add inspection record (201 Created)
- `GET /api/v1/jobs/{id}/inspections` - Get job inspections (200 OK)

**Example Job Creation Request:**
```json
{
  "bookingId": 1,
  "complaintDescription": "Engine making unusual noise",
  "inspectionNotes": "Customer reports knocking sound during acceleration"
}
```

**Example Status Update Request:**
```json
{
  "jobStatus": "IN_SERVICE",
  "notes": "Started working on engine inspection"
}
```

**Example Inspection Record Request:**
```json
{
  "category": "ENGINE",
  "itemName": "Oil Level",
  "condition": "GOOD",
  "notes": "Oil level normal, no leaks detected"
}
```

**Example Job Response:**
```json
{
  "id": 1,
  "jobCardNumber": "JC-20261110-00001",
  "bookingId": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "customerPhone": "1234567890",
  "vehicleId": 1,
  "vehicleRegistrationNumber": "ABC123",
  "vehicleMake": "Toyota",
  "vehicleModel": "Camry",
  "jobStatus": "IN_SERVICE",
  "complaintDescription": "Engine making unusual noise",
  "inspectionNotes": "Customer reports knocking sound",
  "technicianNotes": "Started engine diagnostics",
  "createdAt": "2026-11-10T23:00:00",
  "updatedAt": "2026-11-10T23:15:00",
  "startedAt": "2026-11-10T23:15:00",
  "completedAt": null,
  "deliveredAt": null,
  "inspections": [
    {
      "id": 1,
      "jobId": 1,
      "category": "ENGINE",
      "itemName": "Oil Level",
      "condition": "GOOD",
      "notes": "Oil level normal",
      "inspectedAt": "2026-11-10T23:10:00"
    }
  ]
}
```

**Commits:**
- `8263921` - feat(job): add service job entity and status lifecycle
- `fe18b8a` - feat(job): implement job card creation and booking integration
- `ecad2b7` - test(job): add comprehensive job card test coverage

### ✅ Phase 6: Repair Estimates & Customer Approval (Completed)

Complete repair estimation system with versioning, customer approval workflow, and job integration.

**Features:**
- RepairEstimate entity with line items and versioning
- BigDecimal-based calculation service with HALF_UP rounding
- Zero tax default (configurable, no hardcoded rates)
- Customer approval/rejection workflow with audit trail
- Estimate revision support with SUPERSEDED status
- Historical preservation of all estimates
- Job status integration requiring approved estimate before repair
- Duplicate approval/rejection prevention
- Full test coverage (36 tests)

**Entity Relationships:**
```
RepairEstimate
├── id (Long, Primary Key)
├── estimateNumber (String, Unique, EST-YYYYMMDD-00001)
├── job (ManyToOne → ServiceJob)
├── version (Integer, Incremental versioning)
├── estimateStatus (Enum: DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, SUPERSEDED)
├── subtotal (BigDecimal)
├── taxAmount (BigDecimal)
├── discountAmount (BigDecimal)
├── totalAmount (BigDecimal)
├── notes (String, Optional)
├── createdAt (LocalDateTime)
├── updatedAt (LocalDateTime)
└── items (OneToMany → RepairEstimateItem)

RepairEstimateItem
├── id (Long, Primary Key)
├── estimate (ManyToOne → RepairEstimate)
├── description (String)
├── itemType (Enum: LABOUR, PART)
├── quantity (BigDecimal)
├── unitPrice (BigDecimal)
└── lineTotal (BigDecimal, Calculated server-side)

EstimateApproval
├── id (Long, Primary Key)
├── estimate (ManyToOne → RepairEstimate)
├── decision (Enum: APPROVED, REJECTED)
├── decisionTimestamp (LocalDateTime)
├── rejectionReason (String, Optional)
└── comments (String, Optional)
```

**Estimate Workflow:**
```
DRAFT → PENDING_APPROVAL → APPROVED/REJECTED
                          ↓ (on revision)
                      SUPERSEDED
```

**Business Rules:**
1. Server-side calculation with BigDecimal precision
2. HALF_UP rounding to 2 decimal places
3. Tax rate explicitly provided (defaults to zero)
4. One active estimate per job (latest version)
5. Revisions create new DRAFT with incremented version
6. Old estimates marked SUPERSEDED, preserved for history
7. Duplicate approval/rejection prevented
8. Job cannot proceed to IN_SERVICE without approved estimate

**Endpoints:**
- `POST /api/v1/jobs/{jobId}/estimates` - Create estimate (201 Created)
- `GET /api/v1/jobs/{jobId}/estimates` - Get job estimates (200 OK)
- `GET /api/v1/jobs/{jobId}/estimates/current` - Get current estimate (200 OK)
- `GET /api/v1/jobs/{jobId}/estimates/{id}` - Get estimate by ID (200 OK)
- `PUT /api/v1/jobs/{jobId}/estimates/{id}` - Update estimate (200 OK)
- `POST /api/v1/jobs/{jobId}/estimates/{id}/submit` - Submit for approval (200 OK)
- `POST /api/v1/jobs/{jobId}/estimates/{id}/approve` - Approve estimate (200 OK)
- `POST /api/v1/jobs/{jobId}/estimates/{id}/reject` - Reject estimate (200 OK)
- `POST /api/v1/jobs/{jobId}/estimates/{id}/revise` - Create revision (201 Created)

**Commits:**
- `0f6940d` - feat(estimate): add estimate entities and historical line items
- `e6a0261` - feat(estimate): implement estimate calculation service
- `10a4208` - feat(approval): implement customer approval and revision workflow
- `d7a03e0` - test(estimate): add comprehensive estimate and approval test coverage

### ✅ Phase 7: Technician Management & Service Bay Allocation (Completed)

Complete technician management, service bay allocation, and job assignment system with scheduling conflict prevention.

**Features:**
- Technician entity with specialization and status tracking
- Service bay management with bay types
- Job assignment linking technician, bay, and job
- Scheduling conflict detection and prevention
- Assignment lifecycle (ACTIVE, COMPLETED, CANCELLED)
- Reassignment support with history preservation
- Active/inactive technician and bay management
- Estimate approval validation before assignment
- Full test coverage (42 tests)

**Entity Relationships:**
```
Technician
├── id (Long, Primary Key)
├── technicianCode (String, Unique)
├── firstName, lastName (String)
├── email (String, Unique, Optional)
├── phone (String, Optional)
├── specialization (Enum: ENGINE, BRAKES, ELECTRICAL, BATTERY, GENERAL_SERVICE, TRANSMISSION, etc.)
├── experienceYears (Integer)
├── status (Enum: AVAILABLE, BUSY, ON_LEAVE, INACTIVE)
├── active (Boolean, Default true)
├── createdAt (LocalDateTime)
└── updatedAt (LocalDateTime)

ServiceBay
├── id (Long, Primary Key)
├── bayCode (String, Unique)
├── bayName (String)
├── bayType (Enum: GENERAL, ENGINE, ELECTRICAL, WASHING, INSPECTION, BODY_PAINT)
├── locationDescription (String, Optional)
├── active (Boolean, Default true)
├── createdAt (LocalDateTime)
└── updatedAt (LocalDateTime)

JobAssignment
├── id (Long, Primary Key)
├── job (ManyToOne → ServiceJob)
├── technician (ManyToOne → Technician)
├── bay (ManyToOne → ServiceBay)
├── scheduledStart (LocalDateTime)
├── scheduledEnd (LocalDateTime)
├── assignmentStatus (Enum: ACTIVE, COMPLETED, CANCELLED)
├── notes (String, Optional)
├── assignedAt (LocalDateTime)
├── updatedAt (LocalDateTime)
├── completedAt (LocalDateTime, Optional)
└── cancelledAt (LocalDateTime, Optional)
```

**Business Rules:**
1. Only active technicians can be assigned
2. Only active service bays can be assigned
3. Scheduled end must be after scheduled start
4. Technician cannot have overlapping active assignments
5. Service bay cannot have overlapping active assignments
6. Job cannot have conflicting active assignments
7. Assignment requires approved estimate for jobs in repair stages
8. Reassignment cancels old assignment and creates new one
9. Complete assignment history preserved (no deletions)
10. Assignment lifecycle independent of job status

**Endpoints:**

Technicians:
- `POST /api/v1/technicians` - Create technician (201 Created)
- `GET /api/v1/technicians` - Get all with filtering (200 OK)
- `GET /api/v1/technicians/{id}` - Get by ID (200 OK)
- `PUT /api/v1/technicians/{id}` - Update technician (200 OK)
- `PATCH /api/v1/technicians/{id}/status` - Update status (200 OK)

Service Bays:
- `POST /api/v1/service-bays` - Create bay (201 Created)
- `GET /api/v1/service-bays` - Get all with filtering (200 OK)
- `GET /api/v1/service-bays/{id}` - Get by ID (200 OK)
- `PUT /api/v1/service-bays/{id}` - Update bay (200 OK)
- `PATCH /api/v1/service-bays/{id}/status` - Update status (200 OK)

Job Assignments:
- `POST /api/v1/job-assignments` - Create assignment (201 Created)
- `GET /api/v1/job-assignments` - Get all assignments (200 OK)
- `GET /api/v1/job-assignments/{id}` - Get by ID (200 OK)
- `GET /api/v1/job-assignments/job/{jobId}` - Get job history (200 OK)
- `GET /api/v1/job-assignments/technician/{technicianId}` - Get technician assignments (200 OK)
- `POST /api/v1/job-assignments/{id}/complete` - Complete assignment (200 OK)
- `POST /api/v1/job-assignments/{id}/cancel` - Cancel assignment (200 OK)
- `POST /api/v1/job-assignments/{id}/reassign` - Reassign job (200 OK)

**Commits:**
- `fe6697b` - feat(technician): add technician, service bay and assignment entities with repositories
- `37ec6c1` - feat(assignment): implement technician, bay and assignment services with scheduling conflict prevention
- `4f7818c` - test(assignment): add comprehensive tests for technician, service bay and job assignment

## Test Coverage

**Total Tests: 246** ✅ All Passing

- **Context Tests**: 1
- **Customer Controller Tests**: 9
- **Customer Service Tests**: 14
- **Vehicle Controller Tests**: 13
- **Vehicle Service Tests**: 20
- **Service Item Controller Tests**: 13
- **Service Item Service Tests**: 19
- **Booking Controller Tests**: 14
- **Booking Service Tests**: 19
- **Job Controller Tests**: 19
- **Job Service Tests**: 27
- **Estimate Calculation Tests**: 10
- **Repair Estimate Service Tests**: 18
- **Repair Estimate Controller Tests**: 8
- **Technician Service Tests**: 14
- **Service Bay Service Tests**: 10
- **Job Assignment Service Tests**: 18

### Test Categories

**Customer Tests (23):**
- CRUD operations
- Duplicate phone validation
- Customer not found scenarios
- Pagination
- Input validation
- Vehicle deletion prevention

**Vehicle Tests (34):**
- Vehicle creation with valid customer
- Vehicle creation with nonexistent customer
- Duplicate registration number handling
- Duplicate VIN handling
- Optional VIN support
- Vehicle retrieval by ID and customer
- Pagination
- Vehicle updates
- Ownership change prevention
- Vehicle deletion
- Input validation
- HTTP status codes

**Service Catalogue Tests (32):**
- Service creation with validation
- Duplicate service code handling
- Service code normalization (uppercase)
- Default active status
- Service retrieval by ID and not found
- Pagination for all services
- Active services filtering
- Category filtering
- Service updates
- Update with duplicate code
- Active status preservation during updates
- Status updates (activate/deactivate)
- Soft deletion (deactivation)
- Negative price rejection
- Zero/negative duration rejection

**Booking Tests (33):**
- Successful booking creation
- Multi-service booking
- Customer not found rejection
- Vehicle not found rejection
- Vehicle ownership validation
- Service not found rejection
- Inactive service rejection
- Duplicate service consolidation
- Past booking date rejection
- Correct price calculation
- Correct duration calculation
- Price snapshot preservation
- Booking retrieval by ID
- Customer booking history
- Vehicle booking history
- Pagination
- Valid status transitions (REQUESTED→CONFIRMED, CONFIRMED→CHECKED_IN)
- Invalid status transition rejection
- Booking cancellation
- Already cancelled rejection
- Already checked-in rejection
- HTTP status codes

**Job Card Tests (46):**
- Job creation from CHECKED_IN booking
- Job creation validation (booking status, existence)
- Duplicate job prevention per booking
- Job card number uniqueness
- All 10 status transitions
- Quality check to in-service rework path
- Invalid status transition rejection
- Inspection record creation
- Multiple inspections per job
- Job retrieval by ID, job card number, booking ID
- Vehicle service history
- Pagination
- Timestamp updates (started, completed, delivered)
- Notes updates (inspection and technician)
- Customer not found rejection
- Vehicle not found rejection
- HTTP status codes

**Repair Estimate Tests (36):**
- Estimate creation with line items
- BigDecimal calculation accuracy
- HALF_UP rounding verification
- Zero tax default
- Tax rate application
- Subtotal and total calculation
- Estimate retrieval by job and ID
- Current estimate retrieval
- Estimate updates
- Submit for approval
- Customer approval workflow
- Customer rejection with reason
- Duplicate approval prevention
- Estimate revision creation
- SUPERSEDED status on revision
- Version increment validation
- Historical estimate preservation
- Job integration validation
- Approved estimate requirement

**Technician Tests (14):**
- Technician creation with specialization
- Duplicate code validation
- Duplicate email validation
- Technician retrieval by ID
- Pagination and filtering
- Filter by status
- Filter by specialization
- Active technicians filtering
- Technician updates
- Status updates
- Active/inactive management

**Service Bay Tests (10):**
- Bay creation with type
- Duplicate code validation
- Bay retrieval by ID
- Pagination and filtering
- Filter by bay type
- Active bays filtering
- Bay updates
- Status updates

**Job Assignment Tests (18):**
- Successful assignment creation
- Invalid schedule validation
- Job not found rejection
- Technician not found rejection
- Inactive technician rejection
- Bay not found rejection
- Inactive bay rejection
- Technician scheduling conflict
- Bay scheduling conflict
- Job scheduling conflict
- Assignment retrieval
- Assignment history by job
- Assignment history by technician
- Complete assignment
- Cancel assignment
- Reassignment workflow
- Estimate approval validation
- Assignment status validation

## Build & Test Results

```
[INFO] Tests run: 246, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## API Response Format

### Success Response (Service Item Example)
```json
{
  "id": 1,
  "serviceCode": "OC001",
  "name": "Engine Oil Change",
  "description": "Complete engine oil change with filter replacement",
  "category": "Maintenance",
  "basePrice": 50.00,
  "estimatedDurationMinutes": 30,
  "active": true,
  "createdAt": "2026-11-09T23:00:00",
  "updatedAt": "2026-11-09T23:00:00"
}
```

### Error Response
```json
{
  "message": "Service with code OC001 already exists",
  "statusCode": 409,
  "timestamp": "2026-11-09T23:00:00"
}
```

### API Examples

**Create a Service Item:**
```bash
POST /api/v1/services
Content-Type: application/json

{
  "serviceCode": "OC001",
  "name": "Engine Oil Change",
  "description": "Complete engine oil change with filter replacement",
  "category": "Maintenance",
  "basePrice": 50.00,
  "estimatedDurationMinutes": 30
}
```

**Get Active Services:**
```bash
GET /api/v1/services/active?page=0&size=10&sortBy=name&direction=ASC
```

**Update Service Status:**
```bash
PATCH /api/v1/services/1/status
Content-Type: application/json

{
  "active": false
}
```

**Get Services by Category:**
```bash
GET /api/v1/services/category/Maintenance?page=0&size=20
```

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/serviceflow/backend/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── VehicleController.java
│   │   │   │   ├── ServiceItemController.java
│   │   │   │   └── ServiceBookingController.java
│   │   │   ├── dto/
│   │   │   │   ├── CustomerRequestDto.java
│   │   │   │   ├── CustomerResponseDto.java
│   │   │   │   ├── VehicleRequestDto.java
│   │   │   │   ├── VehicleResponseDto.java
│   │   │   │   ├── ServiceItemRequestDto.java
│   │   │   │   ├── ServiceItemResponseDto.java
│   │   │   │   ├── CustomerSummaryDto.java
│   │   │   │   ├── VehicleSummaryDto.java
│   │   │   │   ├── ServiceBookingRequestDto.java
│   │   │   │   ├── ServiceBookingResponseDto.java
│   │   │   │   ├── BookingServiceItemRequestDto.java
│   │   │   │   └── BookingServiceItemResponseDto.java
│   │   │   ├── entity/
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Vehicle.java
│   │   │   │   ├── ServiceItem.java
│   │   │   │   ├── BookingStatus.java
│   │   │   │   ├── ServiceBooking.java
│   │   │   │   └── BookingServiceItem.java
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── InvalidStatusTransitionException.java
│   │   │   ├── repository/
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── VehicleRepository.java
│   │   │   │   ├── ServiceItemRepository.java
│   │   │   │   └── ServiceBookingRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── VehicleService.java
│   │   │   │   ├── ServiceItemService.java
│   │   │   │   └── ServiceBookingService.java
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/com/serviceflow/backend/
│       │   ├── controller/
│       │   │   ├── CustomerControllerTest.java
│       │   │   ├── VehicleControllerTest.java
│       │   │   ├── ServiceItemControllerTest.java
│       │   │   ├── ServiceBookingControllerTest.java
│       │   │   └── ServiceJobControllerTest.java
│       │   ├── service/
│       │   │   ├── CustomerServiceTest.java
│       │   │   ├── VehicleServiceTest.java
│       │   │   ├── ServiceItemServiceTest.java
│       │   │   ├── ServiceBookingServiceTest.java
│       │   │   └── ServiceJobServiceTest.java
│       │   └── BackendApplicationTests.java
│       └── resources/
│           └── application-test.yaml
└── pom.xml
```

## Setup & Running

### Prerequisites
- Java 21
- Maven 3.6+
- MySQL 9.7

### Database Configuration
Update `application.yaml` with your MySQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/serviceflow
    username: your_username
    password: your_password
```

### Build & Test
```bash
cd backend
./mvnw clean package
```

### Run Application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Security Configuration

**Development Mode**: All API endpoints under `/api/v1/customers`, `/api/v1/vehicles`, `/api/v1/services`, `/api/v1/bookings`, `/api/v1/jobs`, `/api/v1/technicians`, `/api/v1/service-bays`, and `/api/v1/job-assignments` are currently open for development purposes.

⚠️ **Note**: This is temporary development configuration. Phase 9 will implement proper JWT authentication and role-based authorization (RBAC) for production deployment.

## Database Schema

### Customer Table
- `id` (BIGINT, Primary Key)
- `first_name` (VARCHAR 50, NOT NULL)
- `last_name` (VARCHAR 50, NOT NULL)
- `phone` (VARCHAR 15, UNIQUE, NOT NULL)
- `email` (VARCHAR 100, UNIQUE, NOT NULL)
- `address` (VARCHAR 255)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Vehicle Table
- `id` (BIGINT, Primary Key)
- `registration_number` (VARCHAR 20, UNIQUE, NOT NULL)
- `make` (VARCHAR 50, NOT NULL)
- `model` (VARCHAR 50, NOT NULL)
- `manufacturing_year` (INT)
- `fuel_type` (VARCHAR 20)
- `vin` (VARCHAR 17, UNIQUE)
- `customer_id` (BIGINT, Foreign Key → Customer)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Indexes:**
- `idx_vehicle_registration` on `registration_number`
- `idx_vehicle_customer` on `customer_id`
- `idx_vehicle_vin` on `vin`

**Relationships:**
- Vehicle → Customer (ManyToOne)
- Customer → Vehicle (OneToMany)

### Service Items Table
- `id` (BIGINT, Primary Key)
- `service_code` (VARCHAR 20, UNIQUE, NOT NULL)
- `name` (VARCHAR 100, NOT NULL)
- `description` (TEXT)
- `category` (VARCHAR 50, NOT NULL)
- `base_price` (DECIMAL 10,2, NOT NULL)
- `estimated_duration_minutes` (INT, NOT NULL)
- `active` (BOOLEAN, NOT NULL, DEFAULT TRUE)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Indexes:**
- `idx_service_code` on `service_code`
- `idx_service_category` on `category`
- `idx_service_active` on `active`

**Constraints:**
- `uk_service_code` unique constraint on `service_code`

### Service Bookings Table
- `id` (BIGINT, Primary Key)
- `booking_number` (VARCHAR 30, UNIQUE, NOT NULL)
- `customer_id` (BIGINT, Foreign Key → Customer, NOT NULL)
- `vehicle_id` (BIGINT, Foreign Key → Vehicle, NOT NULL)
- `booking_date_time` (TIMESTAMP, NOT NULL)
- `customer_notes` (TEXT)
- `booking_status` (VARCHAR 20, NOT NULL, DEFAULT 'REQUESTED')
- `estimated_total_price` (DECIMAL 10,2, NOT NULL)
- `estimated_total_duration_minutes` (INT, NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Indexes:**
- `idx_booking_number` on `booking_number`
- `idx_booking_customer` on `customer_id`
- `idx_booking_vehicle` on `vehicle_id`
- `idx_booking_status` on `booking_status`
- `idx_booking_datetime` on `booking_date_time`

**Relationships:**
- ServiceBooking → Customer (ManyToOne)
- ServiceBooking → Vehicle (ManyToOne)
- ServiceBooking → BookingServiceItem (OneToMany)

**Constraints:**
- `uk_booking_number` unique constraint on `booking_number`
- `fk_booking_customer` foreign key to customers
- `fk_booking_vehicle` foreign key to vehicles

### Booking Service Items Table (Line Items with Snapshots)
- `id` (BIGINT, Primary Key)
- `booking_id` (BIGINT, Foreign Key → ServiceBooking, NOT NULL)
- `service_item_id` (BIGINT, Foreign Key → ServiceItem, NOT NULL)
- `service_code_snapshot` (VARCHAR 20, NOT NULL)
- `service_name_snapshot` (VARCHAR 100, NOT NULL)
- `unit_price_snapshot` (DECIMAL 10,2, NOT NULL)
- `estimated_duration_minutes_snapshot` (INT, NOT NULL)
- `quantity` (INT, NOT NULL)
- `line_total` (DECIMAL 10,2, NOT NULL)

**Indexes:**
- `idx_booking_item_booking` on `booking_id`
- `idx_booking_item_service` on `service_item_id`

**Relationships:**
- BookingServiceItem → ServiceBooking (ManyToOne)
- BookingServiceItem → ServiceItem (ManyToOne)

**Constraints:**
- `fk_booking_item_booking` foreign key to service_bookings
- `fk_booking_item_service` foreign key to service_items

**Note:** Historical snapshots preserve pricing and duration even if the service catalogue is updated later.

## Git Commit History

```
8e676d6 - test(catalogue): add comprehensive service catalogue tests
f683351 - feat(catalogue): add service item entity and repository
32a2787 - docs: add comprehensive documentation for Phase 1 and Phase 2
5fda144 - test(vehicle): add comprehensive test coverage for vehicle management
cb7344f - feat(vehicle): add vehicle REST endpoints and security config
6f3e33d - feat(vehicle): implement vehicle service and business rules
b16ec39 - feat(vehicle): add vehicle entity and repository
c5f2c6c - test(customer): add comprehensive unit tests for customer management
a29d0ed - feat(customer): add REST endpoints and security configuration
7d646dd - feat(customer): implement DTOs, service layer and exception handling
364363a - feat(customer): add customer entity and repository
2d72b42 - chore: configure MySQL database connection
a43f464 - feat: initialize Spring Boot backend
eada08e - chore: initialize ServiceFlow repository
```

## Next Phases (Planned)

- **Phase 8**: React + TypeScript Frontend
- **Phase 9**: Frontend Integration, JWT Authentication and RBAC
- **Phase 10**: Docker, CI/CD, Deployment Preparation and Final Testing

**Future Enhancements:**
- Parts Inventory Management
- Advanced Reporting and Analytics
- Notification System
- Mobile Application

## Contributing

This is a learning project following a 10-phase development approach. Each phase builds incrementally on previous work with comprehensive testing and proper Git workflow.

## License

MIT
