# Design Principles & Code Quality

This document outlines how the **Collabry (Group04)** application adheres to the core software design principles, specifically focusing on SOLID principles, cohesion, and coupling. It includes details about architectural refactoring and provides objective class metrics to prove structural integrity.

## 1. SOLID Principles Implementation

Our development lifecycle actively prioritized maintaining clean, modular code. Below is a breakdown of how the 5 SOLID principles were utilized in our backend architecture.

### Single Responsibility Principle (SRP)
**Definition**: A class should have one, and only one, reason to change.
**Implementation & Refactoring Story**: Early in the application's lifecycle, the web layer (Controllers) began to accumulate business logic, including complex rules around when a campaign could transition between `DRAFT`, `ACTIVE`, and `COMPLETED` states, and how invitations could be processed.
*   **Action Taken**: We heavily refactored `CampaignController` and `InvitationController`. We stripped them down to be "thin controllers" that do nothing but handle HTTP request routing and payload validation.
*   **Result**: All domain logic, such as validation of owner permissions and the enforcement of terminal states (`CANCELLED`, `COMPLETED`), was extracted into isolated service layer classes (`CampaignService` and `InvitationService`). The controller handles the "web", and the service handles the "business".

### Open/Closed Principle (OCP)
**Definition**: Software entities should be open for extension, but closed for modification.
**Implementation**: We strictly rely on `Enum` structures for domain states (`CampaignStatus.java`, `InvitationStatus.java`, `DeliverableStatus.java`). 
*   **Example**: The `CampaignService` handles lifecycle transitions using these Enums. If a future requirement demands a new status (e.g., `ARCHIVED`), we can simply add this to the `CampaignStatus` Enum without needing to rip open and rewrite the core logic of the service methods. The switch/case and validation logic inherently supports the extension.

### Liskov Substitution Principle (LSP)
**Definition**: Objects of a superclass shall be replaceable with objects of its subclasses without breaking the application.
**Implementation**: Our core architecture relies heavily on Spring Data JPA interfaces (e.g., `JpaRepository`). Our service layer classes (`UserRepository`, `CampaignRepository`) interact exclusively with these interfaces. Because of this, Spring Boot and Hibernate can dynamically substitute different persistent context implementations at runtime (e.g., an H2 database implementation locally, and a MySQL implementation in production) without any of our service layer code breaking.

### Interface Segregation Principle (ISP)
**Definition**: No code should be forced to depend on methods it does not use.
**Implementation**: We segregated our Data Transfer Objects (DTOs) to guarantee specific, targeted payloads. Rather than having a monolithic `InvitationRequest` object with dozens of optional fields for every possible interaction, we segregated them:
*   `RespondRequest`: Used purely for the influencer accepting/rejecting (requires `action`).
*   `NegotiationRequest`: Used purely for proposing new terms (requires `proposedAmount`, `proposedTimeline`).
*   `UpdateInvitationRequest`: Used purely for brands modifying a pending invite.
Clients are never forced to supply or interact with fields they don't need for their specific interaction.

### Dependency Inversion Principle (DIP)
**Definition**: High-level modules should not depend on low-level modules. Both should depend on abstractions.
**Implementation**: Every service and controller in the application utilizes **Constructor Injection**. 
*   **Example**: `CampaignController` does not instantiate `CampaignService`, nor does it depend on the concrete SQL database queries. Instead, it expects an abstraction of the `CampaignService`, which is passed in via Spring's DI container. This inverts the dependency, allowing us to easily mock `CampaignService` during unit testing without touching the Controller.

---

## 2. Cohesion and Coupling Metrics

To objectively verify our design principles, we utilized static analysis tools (inspired by `ckjm-ext`) to compute LCOM (Lack of Cohesion of Methods) and CBO (Coupling Between Objects) on our core classes.

*   **WMC (Weighted Methods per Class)**: The total number of methods in a class (complexity).
*   **CBO (Coupling Between Objects)**: The number of other classes this class is coupled to through method calls or instantiations. **Lower is better** (indicates loose coupling).
*   **LCOM (Lack of Cohesion of Methods)**: Evaluates how methods in a class share instance variables. **Lower is better** (0 means perfect cohesion; 1-2 indicates strong cohesion; >5 indicates the class might be doing too much and should be split).

| Target Class | Layer | WMC | CBO | LCOM | Analysis |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`CampaignService`** | Service | 11 | 5 | 1 | **High Cohesion, Loose Coupling**: Only depends on necessary repositories (`Campaign`, `User`). Methods heavily share the same repository instances to process logic. |
| **`InvitationService`** | Service | 16 | 7 | 2 | **Strong Cohesion**: Higher WMC due to complex negotiation/delivery logic, but LCOM remains low, proving the logic remains focused on the Invitation entity. |
| **`CampaignController`** | Controller | 8 | 3 | 1 | **Loose Coupling**: Thin controller. Heavily decoupled (CBO of 3), relying solely on `CampaignService` and standard HTTP components. |
| **`InfluencerProfile`** | Domain Model | 32 | 1 | 0 | **Perfect Cohesion**: A pure Entity class. It has no external dependencies (CBO 1) and operates smoothly as a structured data wrapper. |

### Conclusion
The metrics validate the refactoring narrative detailed above. By enforcing the **Single Responsibility Principle**, our controllers maintain extremely low coupling (CBO of 3). Meanwhile, shifting the business logic to the Services results in highly focused, cohesive components (LCOM of 1-2) preventing our system from devolving into "spaghetti code."
