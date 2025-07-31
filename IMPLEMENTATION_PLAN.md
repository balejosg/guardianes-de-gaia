# Project Implementation Plan: Guardianes de Gaia

## ‼️ Important: Guiding Principles for Implementation

Before writing any code, it is critical to understand the project's established conventions and architecture. All development must adhere to the principles and specifications outlined in the following documents:

- **Project Vision & Domain:** `docs/PROYECTO.md`
- **Technical Architecture:** `docs/TECH_STACK.md`
- **Domain-Driven Design Model:** `.claude-context/domain-model.md`
- **Naming Conventions:** `docs/UBIQUITOUS_LANGUAGE.md`
- **Feature Breakdown:** `docs/VERTICAL_SLICING_STRATEGY.md`

---

## Objective

To build out the core features required for a functional gameplay loop. This plan prioritizes the implementation of guardian profile management, the card collection backend, and the final integration of the mobile application features.

---

### **Phase 1: Implement Guardian Profile Management (Slice G1)**

This phase establishes the core user identity, which is a prerequisite for all other features.

**1.1. Backend Implementation:**
-   **Data Model:** Create the `Guardian` entity.
    -   *Reference: `.claude-context/domain-model.md` for attributes.*
-   **Persistence:** Implement the `GuardianRepository` for data access.
    -   *Reference: `docs/TECH_STACK.md` for Spring Data JPA conventions.*
-   **Business Logic:** Develop the `GuardianService` for creating and retrieving profiles.
-   **API Layer:** Build the `GuardianController` to expose REST endpoints.
    -   `POST /api/v1/guardians`
    -   `GET /api/v1/guardians/{id}`
    -   *Reference: Existing controllers for style and structure.*

**1.2. Mobile Implementation:**
-   **UI Development:** Construct a `Profile Creation` screen.
-   **API Integration:** Connect the UI to the backend `POST` endpoint.
-   **Session Management:** Securely store the `guardianId` on the device after creation.
    -   *Reference: `docs/TECH_STACK.md` for Flutter state management (BLoC) and secure storage practices.*

---

### **Phase 2: Implement Card Collection Backend (Slice C1)**

This phase builds the missing backend functionality for the card collection feature.

**2.1. Backend Implementation:**
-   **Data Models:**
    -   Create the `Card` entity (master list).
    -   Create the `CollectedCard` entity (links cards to guardians).
    -   *Reference: `.claude-context/domain-model.md` for attributes.*
-   **Persistence:** Implement repositories for `Card` and `CollectedCard`.
-   **Business Logic:** Develop the `CardService` to handle:
    -   QR code validation.
    -   Adding cards to a guardian's collection.
    -   Retrieving a guardian's collection.
-   **API Layer:** Build the `CardController` to expose endpoints:
    -   `POST /api/v1/guardians/{guardianId}/cards/scan`
    -   `GET /api/v1/guardians/{guardianId}/cards`

---

### **Phase 3: Integrate and Finalize Mobile App Features**

This phase connects the existing mobile UI to the fully implemented backend and removes all hardcoded placeholder values.

**3.1. Mobile: Card Feature Integration:**
-   **Dynamic User ID:** Update the `cards` feature (`QRScannerPage`, `CardCollectionPage`) to use the stored `guardianId`.
-   **API Integration:**
    -   Connect `QRScannerPage` to the `scan` endpoint.
    -   Connect `CardCollectionPage` to the `cards` endpoint.
    -   *Reference: Existing mobile repository implementations for style.*

**3.2. Mobile: Walking Feature Integration:**
-   **Dynamic User ID:** Update the `walking` feature (`StepTrackingPage`) to use the stored `guardianId`.
-   **UI Logic:** Complete the client-side validation for manual step entry as noted in the source code's `TODO` comment.
-   **API Integration:** Ensure the `StepTrackingPage` communicates correctly with the backend using the dynamic `guardianId`.
