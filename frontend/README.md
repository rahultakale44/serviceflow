# ServiceFlow Frontend

Modern React + TypeScript frontend for the ServiceFlow auto service management application.

## Technology Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **React Router v6** - Client-side routing
- **Tailwind CSS v3** - Utility-first styling
- **Axios** - HTTP client
- **Recharts** - Data visualization
- **Lucide React** - Icon library

## Prerequisites

- Node.js 16+ and npm
- Backend API running on http://localhost:8080

## Quick Start

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

The default configuration points to `http://localhost:8080/api/v1` for the backend API.

### 3. Start Development Server

```bash
npm run dev
```

The application will be available at http://localhost:5173

### 4. Build for Production

```bash
npm run build
```

The production build will be created in the `dist/` directory.

### 5. Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── public/           # Static assets
├── src/
│   ├── api/         # API client and service functions
│   ├── assets/      # Images and icons
│   ├── components/  # Reusable components
│   ├── layouts/     # Layout components
│   ├── pages/       # Route components
│   ├── types/       # TypeScript type definitions
│   ├── App.tsx      # Main app component with routing
│   ├── main.tsx     # Application entry point
│   └── index.css    # Global styles
├── .env.example     # Environment variables template
├── index.html       # HTML template
├── package.json     # Dependencies and scripts
├── tailwind.config.js  # Tailwind CSS configuration
├── tsconfig.json    # TypeScript configuration
└── vite.config.ts   # Vite configuration
```

## Features

### Implemented Screens

1. **Dashboard** (`/`)
   - Real-time KPI cards (customers, vehicles, bookings, jobs, technicians, service bays)
   - Booking trends bar chart
   - Resource overview pie chart
   - Quick action links

2. **Customers** (`/customers`)
   - Full listing with pagination
   - Search functionality
   - Create, edit, delete operations
   - Detailed customer information display

### Stub Screens (Ready for Implementation)

The following screens have routing and basic structure but need full implementation:

- Vehicles (`/vehicles`)
- Services (`/services`)
- Bookings (`/bookings`)
- Job Cards (`/jobs`)
- Estimates (`/estimates`)
- Technicians (`/technicians`)
- Service Bays (`/service-bays`)
- Job Assignments (`/assignments`)

Each module includes:
- List view page
- Create/Edit form page
- Complete API integration ready

## API Integration

### API Client

The centralized API client (`src/api/client.ts`) provides:
- Base URL configuration from environment variables
- Request/response interceptors
- Global error handling
- Authentication header support (ready for JWT)

### API Services

All backend endpoints are wrapped in typed service functions (`src/api/services.ts`):

```typescript
// Example: Customer API
const customers = await customerApi.getAll({ page: 0, size: 10, sort: 'id,asc' });
const customer = await customerApi.getById(1);
await customerApi.create(customerData);
await customerApi.update(1, customerData);
await customerApi.delete(1);
```

Available services:
- `customerApi` - Customer management
- `vehicleApi` - Vehicle management
- `serviceApi` - Service catalog
- `bookingApi` - Service bookings
- `jobApi` - Job cards
- `estimateApi` - Repair estimates
- `technicianApi` - Technician management
- `serviceBayApi` - Service bay management
- `assignmentApi` - Job assignments

## TypeScript Types

Complete type definitions mirror the backend DTOs in `src/types/index.ts`:

- `Customer`, `Vehicle`, `ServiceItem`
- `ServiceBooking`, `ServiceJob`, `RepairEstimate`
- `Technician`, `ServiceBay`, `JobAssignment`
- All request/response DTOs
- Enums for status fields

## Styling

### Tailwind CSS Configuration

Custom theme with navy sidebar:

```javascript
colors: {
  primary: '#1e40af',
  secondary: '#64748b',
}
```

### Responsive Design

- Mobile-first approach
- Responsive sidebar navigation
- Mobile menu for small screens
- Adaptive tables and forms

## Development Guidelines

### Code Organization

- Keep components small and focused
- Use TypeScript interfaces for all props
- Implement proper error handling
- Show loading states during async operations
- Display empty states when no data exists

### API Error Handling

All API calls handle errors consistently:

```typescript
try {
  const data = await customerApi.getAll();
  setCustomers(data);
} catch (error) {
  setError('Failed to load customers');
}
```

### Form Validation

Forms include client-side validation:
- Required field checks
- Email format validation
- Phone number validation
- Prevents submission of invalid data

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint (if configured)

## Integration with Backend

### CORS Configuration

The backend has been configured to accept requests from http://localhost:5173:

- Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Credentials: enabled
- Headers: all allowed

### Running Both Services

Terminal 1 (Backend):
```bash
cd backend
./mvnw spring-boot:run
# Backend runs on http://localhost:8080
```

Terminal 2 (Frontend):
```bash
cd frontend
npm run dev
# Frontend runs on http://localhost:5173
```

## Phase 8 Status

✅ React + TypeScript application initialized  
✅ Complete API client layer  
✅ All TypeScript types defined  
✅ Responsive layout with navigation  
✅ Dashboard with real data integration  
✅ Customer management fully implemented  
✅ CORS configured in backend  
✅ Build system working  
⏳ Remaining 7 modules ready for implementation

## Next Steps (Future Phases)

1. Implement remaining module screens (Vehicles, Services, Bookings, etc.)
2. Add authentication and JWT support
3. Implement role-based access control
4. Add advanced search and filtering
5. Implement real-time updates
6. Add comprehensive form validation
7. Improve error handling and user feedback
8. Add unit and integration tests
9. Optimize performance and bundle size
10. Implement offline support with service workers

## Troubleshooting

### Backend Connection Issues

If you see API errors:
1. Verify backend is running on http://localhost:8080
2. Check `.env` file has correct `VITE_API_BASE_URL`
3. Ensure CORS is configured in backend SecurityConfig
4. Check browser console for specific error messages

### Build Errors

If build fails:
1. Delete `node_modules` and `package-lock.json`
2. Run `npm install` again
3. Clear Vite cache: `rm -rf node_modules/.vite`
4. Try `npm run build` again

### Port Conflicts

If port 5173 is already in use:
- Vite will automatically try the next available port
- Or specify a different port in `vite.config.ts`

## License

This project is part of the ServiceFlow auto service management system.
