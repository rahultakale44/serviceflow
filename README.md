# ServiceFlow

A comprehensive vehicle service center management platform built with Spring Boot.

## Project Overview

ServiceFlow is a modern backend system for managing vehicle service operations, including customer management, vehicle registration, service scheduling, and billing. Built with industry-standard technologies and best practices.

**Repository**: https://github.com/rahultakale44/serviceflow  
**Branch**: main  
**Status**: Phase 2 Complete ✅

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

## Test Coverage

**Total Tests: 57** ✅ All Passing

- **Context Tests**: 1
- **Customer Controller Tests**: 9
- **Customer Service Tests**: 14
- **Vehicle Controller Tests**: 13
- **Vehicle Service Tests**: 20

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

## Build & Test Results

```
[INFO] Tests run: 57, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 01:41 min
```

## API Response Format

### Success Response
```json
{
  "id": 1,
  "registrationNumber": "ABC123",
  "make": "Toyota",
  "model": "Camry",
  "manufacturingYear": 2022,
  "fuelType": "Petrol",
  "vin": "1HGBH41JXMN109186",
  "customer": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890"
  },
  "createdAt": "2026-11-09T18:30:00",
  "updatedAt": "2026-11-09T18:30:00"
}
```

### Error Response
```json
{
  "message": "Vehicle with registration number ABC123 already exists",
  "statusCode": 409,
  "timestamp": "2026-11-09T18:30:00"
}
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
│   │   │   │   └── VehicleController.java
│   │   │   ├── dto/
│   │   │   │   ├── CustomerRequestDto.java
│   │   │   │   ├── CustomerResponseDto.java
│   │   │   │   ├── VehicleRequestDto.java
│   │   │   │   └── VehicleResponseDto.java
│   │   │   ├── entity/
│   │   │   │   ├── Customer.java
│   │   │   │   └── Vehicle.java
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   └── VehicleRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CustomerService.java
│   │   │   │   └── VehicleService.java
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/com/serviceflow/backend/
│       │   ├── controller/
│       │   │   ├── CustomerControllerTest.java
│       │   │   └── VehicleControllerTest.java
│       │   ├── service/
│       │   │   ├── CustomerServiceTest.java
│       │   │   └── VehicleServiceTest.java
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

**Development Mode**: All API endpoints under `/api/v1/customers` and `/api/v1/vehicles` are currently open for development purposes.

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

## Git Commit History

```
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

- **Phase 3**: Service Catalogue Management
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
