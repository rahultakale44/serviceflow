# Getting Started with ServiceFlow

Complete guide to running the ServiceFlow auto service management application.

## Prerequisites

### Backend Requirements
- Java 21 or higher
- Maven 3.9+
- MySQL 9.7 (or compatible version)

### Frontend Requirements
- Node.js 16+ and npm
- Modern web browser (Chrome, Firefox, Edge, Safari)

## Step 1: Database Setup

### Install MySQL

Download and install MySQL from https://dev.mysql.com/downloads/mysql/

### Create Database

```sql
CREATE DATABASE serviceflow;
CREATE USER 'serviceflow_user'@'localhost' IDENTIFIED BY 'serviceflow_pass';
GRANT ALL PRIVILEGES ON serviceflow.* TO 'serviceflow_user'@'localhost';
FLUSH PRIVILEGES;
```

## Step 2: Backend Setup

### 1. Navigate to Backend Directory

```bash
cd backend
```

### 2. Configure Database Connection

Update `src/main/resources/application.yaml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/serviceflow
    username: serviceflow_user
    password: serviceflow_pass
```

### 3. Run Tests (Optional but Recommended)

```bash
./mvnw test
```

Expected result: **246 tests passing**

### 4. Start Backend Server

```bash
./mvnw spring-boot:run
```

The backend API will be available at: **http://localhost:8080**

### Verify Backend is Running

Open your browser and visit:
- http://localhost:8080/api/v1/customers
- http://localhost:8080/api/v1/services
- http://localhost:8080/api/v1/technicians

You should see JSON responses (empty arrays initially).

## Step 3: Frontend Setup

### 1. Navigate to Frontend Directory

Open a **new terminal window** and run:

```bash
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Configure Environment (Optional)

The default configuration should work. If you need to change the backend URL:

```bash
cp .env.example .env
# Edit .env if needed
```

### 4. Start Development Server

```bash
npm run dev
```

The frontend will be available at: **http://localhost:5173**

## Step 4: Access the Application

Open your browser and navigate to:

**http://localhost:5173**

You should see the ServiceFlow dashboard.

## Quick Start Guide

### 1. Create a Customer

1. Navigate to **Customers** from the sidebar
2. Click **Add Customer**
3. Fill in customer details:
   - First Name: John
   - Last Name: Doe
   - Phone: 1234567890
   - Email: john.doe@example.com
4. Click **Create Customer**

### 2. Add a Vehicle

1. Navigate to **Vehicles** from the sidebar
2. Click **Add Vehicle**
3. Select the customer you just created
4. Fill in vehicle details:
   - Registration Number: ABC123
   - Make: Toyota
   - Model: Camry
   - Manufacturing Year: 2022
   - Fuel Type: Petrol

### 3. Create Services

1. Navigate to **Services** from the sidebar
2. Add services like:
   - Oil Change
   - Brake Inspection
   - Tire Rotation
   - etc.

### 4. Create a Booking

1. Navigate to **Bookings**
2. Click **New Booking**
3. Select customer and vehicle
4. Choose booking date/time
5. Select services
6. Submit booking

## Development Workflow

### Terminal 1: Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Terminal 2: Frontend
```bash
cd frontend
npm run dev
```

### Terminal 3: Run Tests
```bash
cd backend
./mvnw test
```

## API Documentation

### Backend REST APIs

All APIs are available under `/api/v1/`:

- **Customers**: `/api/v1/customers`
- **Vehicles**: `/api/v1/vehicles`
- **Services**: `/api/v1/services`
- **Bookings**: `/api/v1/bookings`
- **Job Cards**: `/api/v1/jobs`
- **Estimates**: `/api/v1/jobs/{jobId}/estimates`
- **Technicians**: `/api/v1/technicians`
- **Service Bays**: `/api/v1/service-bays`
- **Assignments**: `/api/v1/job-assignments`

### Example API Calls

#### Get All Customers
```bash
curl http://localhost:8080/api/v1/customers
```

#### Create Customer
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "email": "john.doe@example.com"
  }'
```

## Troubleshooting

### Backend Issues

**Problem**: Backend won't start - "Connection refused" to MySQL

**Solution**: 
1. Verify MySQL is running: `mysql -u root -p`
2. Check database exists: `SHOW DATABASES;`
3. Verify credentials in `application.yaml`

**Problem**: Tests fail

**Solution**:
1. Delete `target/` directory
2. Run `./mvnw clean test`

### Frontend Issues

**Problem**: Cannot connect to backend API

**Solution**:
1. Verify backend is running on http://localhost:8080
2. Check browser console for CORS errors
3. Verify `.env` file has correct `VITE_API_BASE_URL`

**Problem**: Build fails

**Solution**:
1. Delete `node_modules/` and `package-lock.json`
2. Run `npm install` again
3. Try `npm run build`

**Problem**: Port 5173 is already in use

**Solution**: Vite will automatically try the next available port (5174, 5175, etc.)

## Production Build

### Backend

```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
```

The production build will be in `frontend/dist/`. Serve with any static file server:

```bash
npm run preview
```

## Default Port Configuration

- **Backend API**: http://localhost:8080
- **Frontend Dev Server**: http://localhost:5173
- **MySQL Database**: localhost:3306

## Security Notes

### Current Status (Development)

- ⚠️ No authentication required
- ⚠️ CSRF protection disabled
- ⚠️ All endpoints are public
- ⚠️ CORS enabled for localhost:5173

### Future Implementation (Phase 9)

- ✅ JWT-based authentication
- ✅ Role-based access control
- ✅ Secure password storage
- ✅ Production-ready CORS configuration

## Next Steps

After getting started:

1. Explore the Dashboard to see real-time statistics
2. Create customers and their vehicles
3. Set up your service catalog
4. Start creating bookings
5. Manage job cards and assignments
6. Generate repair estimates

## Support

For issues or questions:
- Check the main [README.md](./README.md) for detailed documentation
- Review [frontend/README.md](./frontend/README.md) for frontend-specific info
- Check the test suite for API usage examples

## Project Status

✅ **Phase 1**: Customer Management  
✅ **Phase 2**: Vehicle Management  
✅ **Phase 3**: Service Catalogue  
✅ **Phase 4**: Service Bookings  
✅ **Phase 5**: Job Cards  
✅ **Phase 6**: Repair Estimates  
✅ **Phase 7**: Technician & Assignment Management  
✅ **Phase 8**: React Frontend  
⏳ **Phase 9**: Authentication & Authorization (Upcoming)  
⏳ **Phase 10**: Deployment & Documentation (Upcoming)  

Enjoy using ServiceFlow! 🚗✨
