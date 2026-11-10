# ServiceFlow

A comprehensive vehicle service center management platform built with Spring Boot.

## Project Overview

ServiceFlow is a modern backend system for managing vehicle service operations, including customer management, vehicle registration, service scheduling, and billing. Built with industry-standard technologies and best practices.

**Repository**: https://github.com/rahultakale44/serviceflow  
**Branch**: main  
**Status**: Phase 3 Complete ✅

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

## Test Coverage

**Total Tests: 89** ✅ All Passing

- **Context Tests**: 1
- **Customer Controller Tests**: 9
- **Customer Service Tests**: 14
- **Vehicle Controller Tests**: 13
- **Vehicle Service Tests**: 20
- **Service Item Controller Tests**: 13
- **Service Item Service Tests**: 19

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

## Build & Test Results

```
[INFO] Tests run: 89, Failures: 0, Errors: 0, Skipped: 0
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
│   │   │   │   └── ServiceItemController.java
│   │   │   ├── dto/
│   │   │   │   ├── CustomerRequestDto.java
│   │   │   │   ├── CustomerResponseDto.java
│   │   │   │   ├── VehicleRequestDto.java
│   │   │   │   ├── VehicleResponseDto.java
│   │   │   │   ├── ServiceItemRequestDto.java
│   │   │   │   └── ServiceItemResponseDto.java
│   │   │   ├── entity/
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Vehicle.java
│   │   │   │   └── ServiceItem.java
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── VehicleRepository.java
│   │   │   │   └── ServiceItemRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── VehicleService.java
│   │   │   │   └── ServiceItemService.java
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/com/serviceflow/backend/
│       │   ├── controller/
│       │   │   ├── CustomerControllerTest.java
│       │   │   ├── VehicleControllerTest.java
│       │   │   └── ServiceItemControllerTest.java
│       │   ├── service/
│       │   │   ├── CustomerServiceTest.java
│       │   │   ├── VehicleServiceTest.java
│       │   │   └── ServiceItemServiceTest.java
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

**Development Mode**: All API endpoints under `/api/v1/customers`, `/api/v1/vehicles`, and `/api/v1/services` are currently open for development purposes.

⚠️ **Note**: This is temporary development configuration. Production deployment will require proper JWT authentication and role-based authorization.

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

- **Phase 4**: Service Appointment Scheduling
- **Phase 5**: Service Job Management
- **Phase 6**: Parts Inventory Management
- **Phase 7**: Invoice and Billing
- **Phase 8**: Reporting and Analytics
- **Phase 9**: Notification System
- **Phase 10**: Final Integration and Deployment

## Contributing

This is a learning project following a 10-phase development approach. Each phase builds incrementally on previous work with comprehensive testing and proper Git workflow.

## License

MIT
