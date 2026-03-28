# Design Principles in Collabry

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [SOLID Principles](#solid-principles)
3. [Cohesion](#cohesion)
4. [Coupling](#coupling)
5. [Other Design Principles](#other-design-principles)
6. [Design Patterns](#design-patterns)
7. [Metrics Summary](#metrics-summary)

---

## Architecture Overview

Collabry follows a standard **layered architecture**:

```
Controllers (9)  →  Services (10)  →  Repositories (8)  →  Models (15)
                                                  ↑
                                 DTOs (25) cross all layers
                         Security / Config cross-cuts everything
```

| Layer | Count | Responsibility |
|---|---|---|
| Controllers | 9 | HTTP handling, request/response mapping |
| Services | 10 | Business logic |
| Repositories | 8 | Data access (Spring Data JPA) |
| Models | 15 | Domain entities + enums |
| DTOs | 25 | Data transfer between layers |

---

## SOLID Principles

### 1. Single Responsibility Principle (SRP)

> A class should have only one reason to change.

---

#### ✅ Good Example — `RatingService`

`RatingService` has exactly one responsibility: managing influencer ratings. Every method in the class relates directly to ratings.

```java
// backend/src/main/java/com/group4/backend/service/RatingService.java
public class RatingService {
    public void submitRating(...)       { ... }  // write a rating
    public List<...> getRatings(...)    { ... }  // read ratings
    public double getAverageRating(...) { ... }  // compute average
    public List<...> getRecentReviews(...){ ... } // fetch recent reviews
}
```

All four methods operate on the same two dependencies (`InfluencerRatingRepository`, `InvitationRepository`) and all relate to the same concept. This class has a single, focused reason to change — if rating business rules change.

---

#### ✅ Good Example — `InfluencerSearchRanker`

A pure utility class with no state and one job — computing a relevance score for influencer search results.

```java
// backend/src/main/java/com/group4/backend/service/InfluencerSearchRanker.java
public class InfluencerSearchRanker {
    public static double relevanceScore(
        InfluencerProfile profile,
        String nicheQuery, String locationQuery,
        Integer minFollowers, Integer maxFollowers
    ) {
        double score = 0;
        // niche match: exact=1000, prefix=500, substring=250
        // location match: +100
        // engagement bonus: up to +50
        // follower centering bonus: up to +40
        return score;
    }
}
```

This class has zero dependencies and a single method. It will only change if scoring logic changes.

---

#### ✅ Good Example — `GroqApiClient`

Handles only communication with the Groq LLM API — no business logic, no data transformation, no database access.

```java
// backend/src/main/java/com/group4/backend/service/GroqApiClient.java
@Component
public class GroqApiClient {
    public boolean isConfigured() { ... }
    public String getChatCompletion(String systemPrompt, String userPrompt) { ... }
    public String getTextCompletion(String systemPrompt, String userPrompt) { ... }
}
```

---

#### ⚠️ SRP Violation — `AuthService`

`AuthService` (232 lines) handles multiple unrelated concerns:

| Method | Responsibility |
|---|---|
| `register()` | User registration + sending emails |
| `confirmEmail()` | Email verification workflow |
| `login()` | JWT authentication |
| `loginWithGoogle()` | OAuth integration |
| `forgotPassword()` | Password reset token creation |
| `resetPassword()` | Password update |

These are at least 4 distinct responsibilities: authentication, registration, OAuth, and password reset. A change to OAuth logic should not risk touching the login logic. This class has 7 constructor-injected dependencies, which is a strong signal of too many responsibilities:

```java
public AuthService(
    UserRepository userRepository,
    PendingSignupRepository pendingSignupRepository,
    JwtUtils jwtUtils,
    AuthenticationManager authenticationManager,
    PasswordResetTokenRepository passwordResetTokenRepository,
    PasswordEncoder passwordEncoder,
    EmailService emailService
) { ... }
```

**Suggested improvement:** Split into `AuthService` (login/JWT), `RegistrationService` (register/confirm), `PasswordResetService` (forgot/reset), `OAuthService` (Google login).

---

#### ⚠️ SRP Violation — `InvitationService`

`InvitationService` (258 lines) mixes invitation creation, status transitions (respond, negotiate, confirm, withdraw), and query operations (history, sent invitations). Each status transition has different actors and rules — they are conceptually independent.

---

### 2. Open/Closed Principle (OCP)

> Software entities should be open for extension but closed for modification.

---

#### ✅ Good Example — `EmailService` Interface

The email sending behaviour is defined as an interface, and new implementations can be added without modifying existing code.

```java
// backend/src/main/java/com/group4/backend/service/EmailService.java
public interface EmailService {
    void sendConfirmationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
}
```

Two implementations exist:

- **`SmtpEmailService`** — real SMTP, activated with `@Primary` + `@ConditionalOnProperty`
- **`ConsoleEmailService`** — logs to console when SMTP is not configured

Adding a third implementation (e.g., SendGrid, SES) requires no changes to `AuthService` or any other class — just a new class implementing `EmailService`. `AuthService` depends on the abstraction, not the implementation.

---

#### ✅ Good Example — JPA Specifications in `InfluencerProfileService`

The search feature uses `JpaSpecificationExecutor`, allowing search criteria to be added without modifying repository or service method signatures.

```java
// backend/src/main/java/com/group4/backend/service/InfluencerProfileService.java
Specification<InfluencerProfile> spec = Specification.where(
    (root, q, cb) -> cb.isTrue(root.get("isComplete"))
);
if (niche != null)
    spec = spec.and((root, q, cb) -> cb.like(...));
if (location != null)
    spec = spec.and((root, q, cb) -> cb.like(...));
// ... more filters added without changing method signature
```

New filter criteria can be added without modifying `InfluencerProfileRepository`.

---

### 3. Liskov Substitution Principle (LSP)

> Subtypes must be substitutable for their base types without altering correctness.

---

#### ✅ Good Example — `EmailService` Implementations

Both `SmtpEmailService` and `ConsoleEmailService` correctly implement the `EmailService` contract. Anywhere `EmailService` is injected (i.e., `AuthService`), either implementation works correctly. The calling code does not need to know which implementation is active.

```java
// AuthService only knows about the interface
private final EmailService emailService;

// Works with either SmtpEmailService or ConsoleEmailService
emailService.sendConfirmationEmail(email, token);
```

---

#### ✅ Good Example — Repository Hierarchy

All repositories extend `JpaRepository<Entity, Long>`. Custom query methods like `findByEmail`, `findByUserId` are additive — they do not break the base contract.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
```

---

### 4. Interface Segregation Principle (ISP)

> Clients should not be forced to depend on interfaces they do not use.

---

#### ✅ Good Example — `EmailService` is Minimal

The `EmailService` interface exposes only two methods. Implementations are not forced to implement unrelated methods.

```java
public interface EmailService {
    void sendConfirmationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
}
```

---

#### ✅ Good Example — Focused DTOs

Each DTO serves a specific use case, avoiding fat objects:

| DTO | Purpose |
|---|---|
| `LoginRequest` | Login only (email, password, rememberMe) |
| `SignupRequest` | Registration only |
| `NegotiationRequest` | Counter-offer only (amount, timeline, deliverables) |
| `RespondRequest` | Accept/reject only (action field) |
| `CollaborationAvailabilityRequest` | Toggle availability only |

For example, `RespondRequest` contains only one field (`action`). A client calling respond does not need to supply irrelevant invitation fields.

---

### 5. Dependency Inversion Principle (DIP)

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

---

#### ✅ Good Example — `AuthService` depends on `EmailService` interface

`AuthService` is a high-level business class. It depends on the `EmailService` abstraction, not on `SmtpEmailService` (the concrete class). Spring resolves the concrete bean at runtime.

```java
// High-level module depends on abstraction
private final EmailService emailService; // interface, not SmtpEmailService

emailService.sendConfirmationEmail(to, token); // works with any implementation
```

---

#### ✅ Good Example — Constructor Injection throughout

All services use constructor injection rather than field injection. This makes dependencies explicit and makes classes testable without Spring context.

```java
// backend/src/main/java/com/group4/backend/service/CampaignService.java
public CampaignService(
    CampaignRepository campaignRepository,
    UserRepository userRepository
) {
    this.campaignRepository = campaignRepository;
    this.userRepository = userRepository;
}
```

---

#### ⚠️ DIP Violation — Controllers depend on `UserRepository` directly

Several controllers (e.g., `CampaignController`, `InvitationController`) directly inject `UserRepository` to resolve the currently authenticated user. Controllers are high-level components that should not depend on the data layer directly.

```java
// Anti-pattern: controller reaching into the data layer
@Autowired
private UserRepository userRepository;

private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).orElseThrow();
}
```

This pattern is repeated in all 9 controllers — a cross-cutting concern that belongs in a centralized resolver.

---

## Cohesion

> Cohesion measures how closely related the responsibilities within a class are.

### LCOM (Lack of Cohesion of Methods)

LCOM measures how many pairs of methods in a class share no instance variables. A lower LCOM is better (more cohesive). LCOM = 0 means all methods use all fields; LCOM = 1 means no methods share any field (worst case).

---

#### LCOM Estimates

| Class | Fields Used | Method Count | LCOM Estimate | Assessment |
|---|---|---|---|---|
| `RatingService` | Both repos used by all methods | 4 | ~0.0 | Excellent |
| `PaymentService` | Both repos used by all methods | 6 | ~0.1 | Excellent |
| `CampaignService` | Both repos used by all methods | 3 | ~0.1 | Excellent |
| `UserService` | Both repos split across methods | 2 | ~0.3 | Good |
| `InfluencerProfileService` | 3 dependencies, all used in search | 4 | ~0.3 | Good |
| `BrandProfileService` | Both repos shared across methods | 4 | ~0.2 | Good |
| `InvitationService` | 5 dependencies, partially shared | 7 | ~0.5 | Moderate |
| `AuthService` | 7 dependencies, each method uses subset | 6 | ~0.7 | Poor |
| `AiRecommendationService` | 4 dependencies, split between AI and mock | 2 | ~0.6 | Poor |

---

#### ✅ High Cohesion Example — `RatingService`

```java
public class RatingService {
    private final InfluencerRatingRepository ratingRepository; // used by ALL methods
    private final InvitationRepository invitationRepository;   // used by ALL methods

    public void submitRating(...)         { ratingRepository.save(...); invitationRepository.findById(...); }
    public List<?> getRatings(...)        { ratingRepository.findByInfluencerId(...); }
    public double getAverageRating(...)   { ratingRepository.findByInfluencerId(...); }
    public List<?> getRecentReviews(...) { ratingRepository.findByInfluencerId(...); }
}
```

All 4 methods use `ratingRepository`. All business logic relates to one concept. LCOM ≈ 0.

---

#### ⚠️ Low Cohesion Example — `AuthService`

In `AuthService`, each method uses a different subset of the 7 injected dependencies:

| Method | Dependencies Used |
|---|---|
| `register()` | `userRepository`, `pendingSignupRepository`, `passwordEncoder`, `emailService` |
| `confirmEmail()` | `pendingSignupRepository`, `userRepository`, `jwtUtils` |
| `login()` | `authenticationManager`, `jwtUtils` |
| `loginWithGoogle()` | `userRepository`, `jwtUtils` |
| `forgotPassword()` | `userRepository`, `passwordResetTokenRepository`, `emailService` |
| `resetPassword()` | `passwordResetTokenRepository`, `passwordEncoder` |

`login()` never touches `pendingSignupRepository` or `passwordResetTokenRepository`. `resetPassword()` never touches `authenticationManager` or `jwtUtils`. These methods are loosely related — they share the concept of "authentication" but operate on completely different data. LCOM ≈ 0.7 (high — poor cohesion).

---

## Coupling

> Coupling measures how much a class depends on other classes.

### Efferent Coupling (Ce) — outgoing dependencies

| Class | Direct Dependencies (Ce) | Assessment |
|---|---|---|
| `InfluencerSearchRanker` | 0 | Excellent |
| `RatingService` | 2 | Excellent |
| `CampaignService` | 2 | Excellent |
| `UserService` | 2 | Excellent |
| `PaymentService` | 2 | Good |
| `BrandProfileService` | 2 | Good |
| `InfluencerProfileService` | 4 | Moderate |
| `AiRecommendationService` | 4 | Moderate |
| `InvitationService` | 5 | High |
| `AuthService` | 7 | Very High |
| `CampaignController` | 5 | High |

---

#### ✅ Low Coupling Example — `CampaignService`

```java
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
}
```

Only two dependencies, both repositories. Changes to unrelated services (e.g., `PaymentService`, `RatingService`) have zero impact on `CampaignService`.

---

#### ✅ Low Coupling Example — `InfluencerSearchRanker`

```java
public class InfluencerSearchRanker {
    // Zero dependencies — pure static utility
    public static double relevanceScore(InfluencerProfile profile, ...) { ... }
}
```

Zero efferent coupling. Can be tested and modified in complete isolation.

---

#### ⚠️ High Coupling Example — `AuthService`

With 7 injected dependencies, `AuthService` is affected by changes in any of:
- `UserRepository`
- `PendingSignupRepository`
- `PasswordResetTokenRepository`
- `JwtUtils`
- `AuthenticationManager`
- `PasswordEncoder`
- `EmailService`

Any change to any of these interfaces ripples into `AuthService`. This is the highest efferent coupling in the backend.

---

### Afferent Coupling (Ca) — incoming dependencies (how many classes depend on this class)

| Class | Used By | Ca | Assessment |
|---|---|---|---|
| `UserRepository` | AuthService, UserService, BrandProfileService, InfluencerProfileService, InvitationService + 5 controllers | 10+ | Core — changes are high risk |
| `JwtUtils` | AuthService, JwtAuthenticationFilter | 2 | Low risk |
| `EmailService` | AuthService only | 1 | Easy to change |
| `InfluencerSearchRanker` | InfluencerProfileService only | 1 | Easy to change |
| `RatingService` | InfluencerProfileService, RatingController | 2 | Low risk |

`UserRepository` has the highest afferent coupling — it is a shared dependency across the application. This is expected for a core data access class, but it means its interface must remain stable.

---

### Package-Level Coupling

The project is organized into the following packages with clean dependency direction:

```
controller  →  service  →  repository  →  model
     ↓              ↓
    dto           dto
```

Controllers never bypass the service layer to call repositories directly (except for the `getCurrentUser()` anti-pattern noted above). Services never call controllers. The dependency direction is top-down and acyclic.

---

## Other Design Principles

### DRY (Don't Repeat Yourself)

#### ⚠️ Violation — `getCurrentUser()` repeated in 9 controllers

The following pattern appears verbatim in every controller:

```java
private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
}
```

This is a cross-cutting concern. A change to how the current user is resolved (e.g., switching from email to user ID in the JWT) requires modifying 9 files.

**Suggested improvement:** Extract to a `@ControllerAdvice` base class or inject a custom `HandlerMethodArgumentResolver` that resolves `User` from the security context automatically.

---

#### ⚠️ Violation — Exception handlers repeated in multiple controllers

`@ExceptionHandler(IllegalArgumentException.class)` returning `400 BAD_REQUEST` is duplicated across multiple controllers. This should be centralized in a single `@ControllerAdvice` class.

---

#### ✅ Good Example — `toResponse()` mapper methods in services

Each service has a private `toResponse()` method that maps from entity to DTO. This avoids duplicating mapping logic across controllers and test code.

```java
// backend/src/main/java/com/group4/backend/service/BrandProfileService.java
private BrandProfileResponse toResponse(BrandProfile profile) {
    BrandProfileResponse response = new BrandProfileResponse();
    response.setId(profile.getId());
    response.setName(profile.getName());
    // ... all fields in one place
    return response;
}
```

---

### Law of Demeter (Principle of Least Knowledge)

> A class should only talk to its immediate friends.

#### ✅ Good Example — Services talk only to repositories

Services interact with repositories but not with other services' repositories. For example, `RatingService` fetches invitations from `InvitationRepository` directly rather than calling `InvitationService` — avoiding deep service chains.

---

#### ✅ Good Example — DTOs prevent deep object graph traversal

Controllers never return raw entities. They return DTOs, which means clients only see the fields explicitly included. This prevents controllers from needing to navigate lazy-loaded JPA relationships.

```java
// Controller returns DTO, not raw Campaign entity
return ResponseEntity.ok(campaignService.create(user, request));
```

---

### Separation of Concerns (SoC)

The application cleanly separates:

| Concern | Where it lives |
|---|---|
| HTTP routing + request validation | Controllers |
| Business rules | Services |
| Data access | Repositories |
| Security (JWT, auth) | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtils` |
| SPA routing | `SpaWebConfig`, `SpaController` |
| Email strategy | `EmailService` + implementations |
| AI integration | `AiRecommendationService`, `GroqApiClient` |
| DB initialization | `DataInitializer`, `DatabaseSeeder` |
| Search scoring | `InfluencerSearchRanker` |

Each concern is isolated in its own class or package. For example, the AI recommendation logic is entirely isolated in `AiRecommendationService` and `GroqApiClient` — removing AI features would require deleting only those two classes.

---

### Fail Fast Principle

Services validate inputs and throw exceptions at the earliest possible point:

```java
// backend/src/main/java/com/group4/backend/service/InvitationService.java
public InvitationResponse createInvitation(...) {
    Campaign campaign = campaignRepository.findById(campaignId)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

    if (!campaign.getUserId().equals(brandUser.getId()))
        throw new IllegalArgumentException("You do not own this campaign");

    User influencer = userRepository.findById(influencerId)
        .orElseThrow(() -> new IllegalArgumentException("Influencer not found"));

    boolean alreadyInvited = invitationRepository
        .findByCampaignIdAndInfluencerId(campaignId, influencerId)
        .stream()
        .anyMatch(i -> i.getStatus() == PENDING || i.getStatus() == NEGOTIATING);

    if (alreadyInvited)
        throw new IllegalArgumentException("Influencer already has a pending invitation");
    // ... proceed only when all checks pass
}
```

Validation happens at the top of the method before any state is changed.

---

### Immutability and Defensive Design

#### ✅ Audit Timestamps via JPA Lifecycle Callbacks

Entities use `@PrePersist` and `@PreUpdate` to set timestamps — these cannot be tampered with by callers.

```java
// backend/src/main/java/com/group4/backend/model/BrandProfile.java
@PrePersist
protected void onCreate() { createdAt = LocalDateTime.now(); }

@PreUpdate
protected void onUpdate() { updatedAt = LocalDateTime.now(); }
```

#### ✅ Invoice Numbers generated internally

`PaymentService` generates invoice numbers via `UUID` internally — callers cannot supply their own:

```java
payment.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
```

---

## Design Patterns

### Strategy Pattern — Email Service

```
«interface»
EmailService
    ├── SmtpEmailService   (@Primary, @ConditionalOnProperty)
    └── ConsoleEmailService (fallback)
```

`AuthService` depends only on `EmailService`. At runtime, Spring injects either `SmtpEmailService` (when `spring.mail.host` is configured) or `ConsoleEmailService` (when it is not). This allows the email mechanism to be swapped without any change to business logic.

---

### Repository Pattern

All data access is abstracted behind Spring Data JPA repository interfaces. Services never write raw SQL or use `EntityManager` directly.

```java
public interface InfluencerProfileRepository
    extends JpaRepository<InfluencerProfile, Long>,
            JpaSpecificationExecutor<InfluencerProfile> {
    Optional<InfluencerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
```

---

### Specification / Query Object Pattern

Dynamic search queries are built using JPA `Specification` objects rather than multiple repository methods:

```java
// backend/src/main/java/com/group4/backend/service/InfluencerProfileService.java
Specification<InfluencerProfile> spec = Specification
    .where(isComplete())
    .and(nicheContains(niche))
    .and(locationContains(location))
    .and(followersAtLeast(minFollowers))
    .and(engagementAtLeast(minEngagement))
    .and(isAvailable(availableOnly));

List<InfluencerProfile> results = influencerProfileRepository.findAll(spec);
```

This allows any combination of filters without creating a separate repository method for each case.

---

### DTO Pattern

The application strictly separates internal entities from external API contracts via DTOs. Clients never receive raw JPA entities. Examples:

- `InfluencerProfileResponse` includes `averageRating` and `recentReviews` computed at query time — fields not in the `InfluencerProfile` entity.
- `InvitationDetailResponse` nests a `CampaignResponse` — a composed view not directly in the database.
- `PaymentResponse` includes `campaignName` fetched from a separate table.

---

### Conditional Bean Pattern

Spring's `@ConditionalOnProperty` is used to activate components based on environment configuration:

```java
@Service
@Primary
@ConditionalOnProperty(name = "spring.mail.host")
public class SmtpEmailService implements EmailService { ... }

@Service
@ConditionalOnMissingBean(SmtpEmailService.class)
public class ConsoleEmailService implements EmailService { ... }
```

This allows the same codebase to run locally (console emails) and in production (real SMTP) without code changes.

---

### Template Method / CommandLineRunner

`DataInitializer` and `DatabaseSeeder` both implement `CommandLineRunner`. Spring calls `run()` on each after the context starts. Each class defines its own seed logic, following the Template Method idea where Spring provides the structure and subclasses fill in the behavior.

Both guard against duplicate insertion:

```java
// backend/src/main/java/com/group4/backend/config/DatabaseSeeder.java
if (!userRepository.existsByEmail("admin@brand.com")) {
    // insert only once
}
```

---

## Metrics Summary

### LCOM (Lack of Cohesion of Methods) — lower is better

| Class | LCOM Estimate | Rating |
|---|---|---|
| `InfluencerSearchRanker` | 0.0 | Excellent |
| `RatingService` | ~0.0 | Excellent |
| `CampaignService` | ~0.1 | Excellent |
| `PaymentService` | ~0.1 | Excellent |
| `BrandProfileService` | ~0.2 | Good |
| `UserService` | ~0.3 | Good |
| `InfluencerProfileService` | ~0.3 | Good |
| `AiRecommendationService` | ~0.6 | Poor |
| `InvitationService` | ~0.5 | Moderate |
| `AuthService` | ~0.7 | Poor |

### Efferent Coupling (Ce) — lower is better

| Class | Ce | Rating |
|---|---|---|
| `InfluencerSearchRanker` | 0 | Excellent |
| `RatingService` | 2 | Excellent |
| `CampaignService` | 2 | Excellent |
| `PaymentService` | 2 | Good |
| `BrandProfileService` | 2 | Good |
| `UserService` | 2 | Good |
| `InfluencerProfileService` | 4 | Moderate |
| `AiRecommendationService` | 4 | Moderate |
| `InvitationService` | 5 | High |
| `AuthService` | 7 | Very High |

### Class Size (Lines of Code)

| Class | LOC | Assessment |
|---|---|---|
| `InvitationService` | 258 | Too large |
| `AuthService` | 232 | Too large |
| `AiRecommendationService` | 137 | Acceptable |
| `InfluencerProfileService` | 191 | Borderline |
| `PaymentService` | 150 | Acceptable |
| `RatingService` | 81 | Good |
| `CampaignService` | 87 | Good |
| `UserService` | 45 | Excellent |

### SOLID Compliance Summary

| Principle | Compliance | Key Issue |
|---|---|---|
| Single Responsibility | Partial | `AuthService`, `InvitationService` have multiple responsibilities |
| Open/Closed | Good | `EmailService` strategy + JPA Specifications extensible |
| Liskov Substitution | Good | `EmailService` implementations are substitutable |
| Interface Segregation | Good | DTOs and `EmailService` are minimal |
| Dependency Inversion | Partial | Services depend on `EmailService` interface; controllers couple to `UserRepository` directly |
