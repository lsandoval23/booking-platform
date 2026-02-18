# 🤔 Shared Models vs. Isolated Models - Architecture Decision

## The Question

**Why use shared modules (`shared/common-models`, `shared/common-utils`) instead of having each component handle its own models?**

This is an excellent question that touches on a fundamental microservices architecture debate: **Shared Libraries vs. Complete Service Autonomy**.

---

## 📊 Comparison: Shared vs. Isolated

### Approach 1: Shared Models (Current Design)

```
booking-platform/
├── shared/
│   ├── common-models/          # Shared across services
│   │   ├── User.java
│   │   ├── Resource.java
│   │   ├── Booking.java
│   │   └── Payment.java
│   └── common-utils/           # Shared utilities
│       ├── JwtUtil.java
│       └── ValidationUtil.java
│
├── services/
│   ├── auth-service/           # Uses shared models
│   │   └── depends on: common-models, common-utils
│   ├── booking-service/        # Uses shared models
│   │   └── depends on: common-models, common-utils
│   └── payment-service/        # Uses shared models
│       └── depends on: common-models, common-utils
```

### Approach 2: Isolated Models (Alternative)

```
booking-platform/
├── services/
│   ├── auth-service/
│   │   └── models/
│   │       ├── User.java           # Own User model
│   │       └── RefreshToken.java
│   │
│   ├── booking-service/
│   │   └── models/
│   │       ├── Booking.java        # Own Booking model
│   │       ├── User.java           # Duplicate User model
│   │       └── Resource.java       # Duplicate Resource model
│   │
│   └── payment-service/
│       └── models/
│           ├── Payment.java        # Own Payment model
│           ├── Booking.java        # Duplicate Booking model
│           └── User.java           # Duplicate User model
```

---

## ⚖️ Trade-offs Analysis

### Shared Models Approach

#### ✅ Advantages

1. **DRY Principle (Don't Repeat Yourself)**
   - Single source of truth for domain models
   - No code duplication
   - Easier to maintain consistency

2. **Faster Development**
   - No need to redefine models in each service
   - Immediate access to all domain entities
   - Less boilerplate code

3. **Type Safety Across Services**
   - Compile-time validation of data contracts
   - IDE autocomplete and refactoring support
   - Catch errors early

4. **Easier Refactoring**
   - Change model once, affects all services
   - Centralized validation logic
   - Consistent business rules

5. **Simplified Event Schemas**
   - Events use shared models
   - No serialization/deserialization mismatches
   - Guaranteed compatibility

6. **Monorepo Benefits**
   - All code in one repository
   - Atomic commits across services
   - Easier to track changes

#### ❌ Disadvantages

1. **Tight Coupling**
   - Services depend on shared library
   - Changes to shared models affect all services
   - Harder to deploy services independently

2. **Versioning Challenges**
   - Breaking changes require coordinated updates
   - All services must use same version
   - Difficult to maintain backward compatibility

3. **Build Dependencies**
   - Must build shared library first
   - Longer build times
   - Circular dependency risks

4. **Service Autonomy Loss**
   - Services can't evolve models independently
   - Forced to include unnecessary fields
   - Violates microservices principle of independence

5. **Deployment Coupling**
   - Shared library update requires redeploying all services
   - Increased risk of breaking changes
   - Harder to rollback

---

### Isolated Models Approach

#### ✅ Advantages

1. **Service Autonomy**
   - Each service owns its data model
   - Can evolve independently
   - No shared dependencies

2. **Independent Deployment**
   - Deploy services without affecting others
   - No coordination needed
   - Easier rollback

3. **Bounded Contexts (DDD)**
   - Each service has its own domain model
   - Models reflect service-specific needs
   - Clear service boundaries

4. **Versioning Freedom**
   - Services can use different model versions
   - Backward compatibility easier
   - Gradual migration possible

5. **Technology Flexibility**
   - Different services can use different languages
   - No shared library constraints
   - Polyglot architecture possible

6. **Failure Isolation**
   - Model changes don't cascade
   - Reduced blast radius
   - Better fault tolerance

#### ❌ Disadvantages

1. **Code Duplication**
   - Same models defined multiple times
   - Inconsistent implementations
   - More code to maintain

2. **Synchronization Overhead**
   - Must keep models in sync manually
   - Risk of drift between services
   - Harder to ensure consistency

3. **Integration Complexity**
   - Need API contracts (OpenAPI, gRPC)
   - Data transformation between services
   - More serialization/deserialization

4. **Development Overhead**
   - More boilerplate code
   - Slower initial development
   - Duplicate validation logic

5. **Testing Complexity**
   - Must test data contracts
   - Integration tests more complex
   - Contract testing required

---

## 🎯 Recommended Hybrid Approach

The best solution is **neither pure shared nor pure isolated**, but a **hybrid approach** based on coupling levels.

### Strategy: Shared Core, Isolated Specifics

```
booking-platform/
├── shared/
│   ├── contracts/              # API contracts (interfaces/DTOs)
│   │   ├── UserDTO.java        # Data Transfer Objects
│   │   ├── BookingDTO.java
│   │   └── ResourceDTO.java
│   │
│   ├── events/                 # Event schemas (shared)
│   │   ├── BookingCreatedEvent.java
│   │   └── PaymentProcessedEvent.java
│   │
│   └── common-utils/           # Truly common utilities
│       ├── JwtUtil.java
│       └── DateUtil.java
│
├── services/
│   ├── auth-service/
│   │   └── domain/             # Internal domain models
│   │       ├── User.java       # Rich domain model
│   │       └── RefreshToken.java
│   │
│   ├── booking-service/
│   │   └── domain/
│   │       ├── Booking.java    # Own Booking model
│   │       ├── UserReference.java  # Lightweight reference
│   │       └── ResourceReference.java
│   │
│   └── payment-service/
│       └── domain/
│           ├── Payment.java
│           └── BookingReference.java  # Reference, not full model
```

### Rules for Hybrid Approach

#### ✅ Share These:

1. **API Contracts (DTOs)**
   - Data Transfer Objects for inter-service communication
   - Versioned interfaces
   - No business logic

2. **Event Schemas**
   - Event definitions for pub/sub
   - Immutable event structures
   - Versioned events

3. **Common Utilities**
   - JWT handling
   - Date/time utilities
   - Validation helpers
   - No domain logic

4. **Cross-Cutting Concerns**
   - Logging utilities
   - Exception handling
   - Security utilities

#### ❌ Don't Share These:

1. **Domain Models**
   - Rich domain objects with business logic
   - Database entities
   - Service-specific models

2. **Business Logic**
   - Service-specific rules
   - Validation logic
   - State management

3. **Database Schemas**
   - Each service owns its schema
   - No shared tables
   - Use references (IDs) instead

---

## 💡 Practical Example: Booking Service

### ❌ Bad: Shared Domain Model

```java
// shared/common-models/Booking.java
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private UUID id;
    
    @ManyToOne
    private User user;              // Full User object
    
    @ManyToOne
    private Resource resource;      // Full Resource object
    
    @OneToOne
    private Payment payment;        // Full Payment object
    
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BookingStatus status;
    
    // Business logic
    public void confirm() { ... }
    public void cancel() { ... }
}
```

**Problems**:
- Booking service depends on User, Resource, Payment models
- Changes to User model affect Booking service
- Tight coupling across services

---

### ✅ Good: Isolated Domain with Shared Contracts

```java
// shared/contracts/BookingDTO.java (API contract)
public record BookingDTO(
    UUID id,
    UUID userId,           // Just ID, not full User
    UUID resourceId,       // Just ID, not full Resource
    LocalDateTime startDatetime,
    LocalDateTime endDatetime,
    String status
) {}

// services/booking-service/domain/Booking.java (Internal model)
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private UUID id;
    
    private UUID userId;           // Reference only
    private UUID resourceId;       // Reference only
    private UUID paymentId;        // Reference only
    
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    
    // Business logic specific to Booking service
    public void confirm() {
        if (this.status != BookingStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending bookings");
        }
        this.status = BookingStatus.CONFIRMED;
    }
    
    public void cancel() {
        if (this.status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed bookings");
        }
        this.status = BookingStatus.CANCELLED;
    }
    
    // Convert to DTO for API responses
    public BookingDTO toDTO() {
        return new BookingDTO(
            this.id,
            this.userId,
            this.resourceId,
            this.startDatetime,
            this.endDatetime,
            this.status.name()
        );
    }
}

// services/booking-service/service/BookingService.java
@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ResourceServiceClient resourceClient;  // REST client
    
    @Autowired
    private UserServiceClient userClient;
    
    public BookingDTO createBooking(CreateBookingRequest request) {
        // Validate user exists (via API call)
        UserDTO user = userClient.getUser(request.userId());
        if (user == null) {
            throw new UserNotFoundException();
        }
        
        // Validate resource exists and is available (via API call)
        ResourceDTO resource = resourceClient.getResource(request.resourceId());
        if (!resource.isAvailable(request.startDatetime(), request.endDatetime())) {
            throw new ResourceNotAvailableException();
        }
        
        // Create booking with references only
        Booking booking = new Booking();
        booking.setUserId(request.userId());
        booking.setResourceId(request.resourceId());
        booking.setStartDatetime(request.startDatetime());
        booking.setEndDatetime(request.endDatetime());
        booking.setStatus(BookingStatus.PENDING);
        
        booking = bookingRepository.save(booking);
        
        return booking.toDTO();
    }
}
```

**Benefits**:
- Booking service owns its domain model
- Uses references (IDs) instead of full objects
- Communicates via DTOs
- Can evolve independently

---

## 🔄 Communication Patterns

### Pattern 1: API Calls with DTOs

```java
// Booking Service needs user info
@Service
public class BookingService {
    
    @Autowired
    private UserServiceClient userClient;
    
    public BookingDetailsDTO getBookingDetails(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        
        // Fetch user details via API
        UserDTO user = userClient.getUser(booking.getUserId());
        
        // Fetch resource details via API
        ResourceDTO resource = resourceClient.getResource(booking.getResourceId());
        
        return new BookingDetailsDTO(
            booking.toDTO(),
            user,
            resource
        );
    }
}
```

### Pattern 2: Event-Driven with Shared Events

```java
// shared/events/BookingCreatedEvent.java
public record BookingCreatedEvent(
    UUID eventId,
    String eventType,
    Instant timestamp,
    BookingEventData data
) {}

public record BookingEventData(
    UUID bookingId,
    UUID userId,
    UUID resourceId,
    LocalDateTime startDatetime,
    LocalDateTime endDatetime,
    BigDecimal totalPrice
) {}

// Booking Service publishes event
@Service
public class BookingService {
    
    @Autowired
    private SnsClient snsClient;
    
    public void createBooking(CreateBookingRequest request) {
        Booking booking = // ... create booking
        
        // Publish event with shared schema
        BookingCreatedEvent event = new BookingCreatedEvent(
            UUID.randomUUID(),
            "BookingCreated",
            Instant.now(),
            new BookingEventData(
                booking.getId(),
                booking.getUserId(),
                booking.getResourceId(),
                booking.getStartDatetime(),
                booking.getEndDatetime(),
                booking.getTotalPrice()
            )
        );
        
        snsClient.publish(topicArn, objectMapper.writeValueAsString(event));
    }
}

// Notification Lambda consumes event
public class NotificationHandler {
    
    public void handleBookingCreated(BookingCreatedEvent event) {
        // Event schema is shared, so type-safe
        String message = String.format(
            "Booking %s created for user %s",
            event.data().bookingId(),
            event.data().userId()
        );
        
        sendNotification(message);
    }
}
```

---

## 📋 Updated Recommendation for Booking Platform

### Revised Shared Structure

```
booking-platform/
├── shared/
│   ├── api-contracts/          # DTOs for REST APIs
│   │   ├── src/main/java/com/booking/contracts/
│   │   │   ├── user/
│   │   │   │   ├── UserDTO.java
│   │   │   │   └── CreateUserRequest.java
│   │   │   ├── resource/
│   │   │   │   ├── ResourceDTO.java
│   │   │   │   └── AvailabilityDTO.java
│   │   │   ├── booking/
│   │   │   │   ├── BookingDTO.java
│   │   │   │   └── CreateBookingRequest.java
│   │   │   └── payment/
│   │   │       ├── PaymentDTO.java
│   │   │       └── ProcessPaymentRequest.java
│   │   └── pom.xml
│   │
│   ├── event-schemas/          # Event definitions
│   │   ├── src/main/java/com/booking/events/
│   │   │   ├── BookingCreatedEvent.java
│   │   │   ├── BookingConfirmedEvent.java
│   │   │   ├── PaymentProcessedEvent.java
│   │   │   └── EventMetadata.java
│   │   └── pom.xml
│   │
│   └── common-utils/           # Utilities only
│       ├── src/main/java/com/booking/utils/
│       │   ├── JwtUtil.java
│       │   ├── DateUtil.java
│       │   └── ValidationUtil.java
│       └── pom.xml
│
├── services/
│   ├── auth-service/
│   │   └── src/main/java/com/booking/auth/
│   │       ├── domain/         # Internal domain models
│   │       │   ├── User.java
│   │       │   └── RefreshToken.java
│   │       ├── repository/
│   │       ├── service/
│   │       └── controller/     # Uses DTOs from api-contracts
│   │
│   ├── booking-service/
│   │   └── src/main/java/com/booking/booking/
│   │       ├── domain/
│   │       │   ├── Booking.java        # Own model
│   │       │   └── BookingStatus.java
│   │       ├── client/         # REST clients for other services
│   │       │   ├── UserServiceClient.java
│   │       │   └── ResourceServiceClient.java
│   │       ├── repository/
│   │       ├── service/
│   │       └── controller/
│   │
│   └── payment-service/
│       └── src/main/java/com/booking/payment/
│           ├── domain/
│           │   ├── Payment.java
│           │   └── PaymentStatus.java
│           └── ...
```

---

## 🎯 Final Recommendation

### For This Booking Platform: Use Hybrid Approach

1. **Share**:
   - ✅ API contracts (DTOs)
   - ✅ Event schemas
   - ✅ Common utilities (JWT, validation)

2. **Don't Share**:
   - ❌ Domain models (entities)
   - ❌ Business logic
   - ❌ Database schemas

3. **Communication**:
   - REST APIs with DTOs for synchronous calls
   - Events with shared schemas for async communication
   - References (IDs) instead of full objects

### Why This Works

1. **Balance**: Gets benefits of both approaches
2. **Flexibility**: Services can evolve independently
3. **Type Safety**: Shared contracts prevent integration errors
4. **Maintainability**: Clear boundaries, less duplication
5. **Monorepo Friendly**: Works well in single repository
6. **Gradual Migration**: Can extract services later if needed

---

## 📊 Decision Matrix

| Aspect | Shared Models | Isolated Models | Hybrid (Recommended) |
|--------|---------------|-----------------|----------------------|
| **Development Speed** | ⭐⭐⭐⭐⭐ Fast | ⭐⭐ Slow | ⭐⭐⭐⭐ Fast |
| **Service Autonomy** | ⭐ Low | ⭐⭐⭐⭐⭐ High | ⭐⭐⭐⭐ High |
| **Code Duplication** | ⭐⭐⭐⭐⭐ None | ⭐ High | ⭐⭐⭐⭐ Low |
| **Type Safety** | ⭐⭐⭐⭐⭐ High | ⭐⭐ Low | ⭐⭐⭐⭐ High |
| **Independent Deploy** | ⭐ Hard | ⭐⭐⭐⭐⭐ Easy | ⭐⭐⭐⭐ Easy |
| **Versioning** | ⭐ Hard | ⭐⭐⭐⭐⭐ Easy | ⭐⭐⭐⭐ Easy |
| **Maintenance** | ⭐⭐⭐⭐ Easy | ⭐⭐ Hard | ⭐⭐⭐⭐ Easy |
| **Bounded Contexts** | ⭐ Violated | ⭐⭐⭐⭐⭐ Clear | ⭐⭐⭐⭐ Clear |

---

## 🚀 Migration Path

If you start with shared models and want to migrate to isolated:

### Step 1: Extract API Contracts
```bash
# Create api-contracts module
mkdir -p shared/api-contracts/src/main/java/com/booking/contracts

# Move DTOs from common-models to api-contracts
# Keep domain models in services
```

### Step 2: Update Services
```java
// Before: Service uses shared domain model
@Service
public class BookingService {
    public Booking createBooking(Booking booking) { ... }
}

// After: Service uses internal model, exposes DTO
@Service
public class BookingService {
    public BookingDTO createBooking(CreateBookingRequest request) {
        Booking booking = // internal model
        return booking.toDTO();
    }
}
```

### Step 3: Add REST Clients
```java
// Add Feign or RestTemplate clients
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable UUID id);
}
```

### Step 4: Gradual Migration
- Migrate one service at a time
- Keep backward compatibility
- Update consumers gradually

---

## 📝 Summary

**Question**: Why shared models?

**Answer**: For **faster development** and **type safety** in a monorepo, but the **hybrid approach** is better:

- **Share**: API contracts, event schemas, utilities
- **Isolate**: Domain models, business logic, database schemas
- **Communicate**: Via DTOs and events, not shared domain objects

This gives you the best of both worlds: fast development with proper service boundaries.

---

**Document Version**: 1.0  
**Last Updated**: 2026-02-01  
**Author**: Architecture Team
