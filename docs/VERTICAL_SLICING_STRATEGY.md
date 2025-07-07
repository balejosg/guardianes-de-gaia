# Vertical Slicing Strategy for Guardianes de Gaia

## Overview
This document defines the vertical slicing approach for implementing all features in the Guardianes de Gaia project. Each vertical slice represents a complete user feature that spans from the mobile UI to the backend persistence layer.

## Core Principles

### 1. User-Centric Slicing
Each slice delivers a complete user story that provides immediate value to families using the app.

### 2. End-to-End Implementation
Every slice includes:
- Mobile UI components (Flutter)
- Backend API endpoints (Spring Boot)
- Database schema (MySQL)
- Domain logic implementation
- Integration tests

### 3. Incremental Delivery
Slices are prioritized by business value and can be delivered independently.

## Domain-Specific Vertical Slices

### Walking Domain Slices

#### Slice W1: Basic Step Tracking
**User Story**: As a Guardian, I want to track my daily steps so I can generate energy for battles.

**Implementation Stack**:
- **Mobile**: Step counter widget, daily progress display
- **Backend**: Step tracking API, energy calculation service
- **Database**: Steps table, energy_transactions table
- **Domain**: Step aggregation, energy generation rules

**Acceptance Criteria**:
- Guardian can view current step count
- Steps automatically convert to energy (1 energy = 10 steps)
- Daily step history is persisted

#### Slice W2: Magic Route Creation
**User Story**: As a Guía del Pacto, I want to create custom magic routes so my family has structured walking goals.

**Implementation Stack**:
- **Mobile**: Route creation form, map integration
- **Backend**: Route management API, GPS validation
- **Database**: Routes table, route_waypoints table
- **Domain**: Route validation, bonus calculation

**Acceptance Criteria**:
- Parents can create named routes with waypoints
- Routes have start/end points and bonus energy multipliers
- Routes are shared within the Pacto

#### Slice W3: Route Completion Tracking
**User Story**: As a Guardian, I want to complete magic routes and receive bonus energy.

**Implementation Stack**:
- **Mobile**: Route tracking screen, GPS monitoring
- **Backend**: Route completion API, bonus calculation
- **Database**: Route_completions table
- **Domain**: Proximity detection, bonus energy calculation

**Acceptance Criteria**:
- GPS tracking during route walks
- Automatic route completion detection
- Bonus energy awarded upon completion

### Battle Domain Slices

#### Slice B1: Basic Battle Mechanics
**User Story**: As a Guardian, I want to engage in card battles using my energy.

**Implementation Stack**:
- **Mobile**: Battle screen, card selection UI
- **Backend**: Battle engine API, turn management
- **Database**: Battles table, battle_moves table
- **Domain**: Battle rules, energy consumption

**Acceptance Criteria**:
- Guardian can initiate battles
- Energy is consumed during battles
- Battle outcomes are determined by card stats

#### Slice B2: Daily Challenge System
**User Story**: As a Guardian, I want to complete daily challenges to earn rewards.

**Implementation Stack**:
- **Mobile**: Challenge list, progress tracking
- **Backend**: Challenge generation API, completion tracking
- **Database**: Challenges table, challenge_completions table
- **Domain**: Challenge types, reward calculation

**Acceptance Criteria**:
- 5 daily challenges generated automatically
- Progress tracked in real-time
- Rewards distributed upon completion

#### Slice B3: Cooperative Battle Mode
**User Story**: As a Pacto, we want to battle together against AI opponents.

**Implementation Stack**:
- **Mobile**: Multi-player battle UI, Pacto coordination
- **Backend**: Cooperative battle API, shared resources
- **Database**: Pacto_battles table, shared_resources table
- **Domain**: Cooperative mechanics, shared energy pools

**Acceptance Criteria**:
- Pacto members can join battles together
- Shared energy pool for cooperative battles
- Victory rewards distributed to all participants

### Card Domain Slices

#### Slice C1: QR Code Card Collection
**User Story**: As a Guardian, I want to scan QR codes to collect new cards.

**Implementation Stack**:
- **Mobile**: QR scanner, card collection UI
- **Backend**: Card validation API, collection tracking
- **Database**: Cards table, guardian_cards table
- **Domain**: Card rarity, collection rules

**Acceptance Criteria**:
- QR codes validate against card database
- Cards added to Guardian's collection
- Duplicate handling and trade system

#### Slice C2: Deck Management
**User Story**: As a Guardian, I want to organize my cards into battle decks.

**Implementation Stack**:
- **Mobile**: Deck builder UI, card filtering
- **Backend**: Deck management API, validation
- **Database**: Decks table, deck_cards table
- **Domain**: Deck composition rules, card limits

**Acceptance Criteria**:
- Create custom decks from card collection
- Deck validation (size, rarity limits)
- Multiple deck presets

#### Slice C3: NFC Premium Cards
**User Story**: As a Guardian, I want to use NFC cards for premium collection experience.

**Implementation Stack**:
- **Mobile**: NFC reader integration, premium UI
- **Backend**: NFC validation API, premium features
- **Database**: Premium_cards table, nfc_codes table
- **Domain**: Premium card benefits, special abilities

**Acceptance Criteria**:
- NFC cards register with enhanced stats
- Premium card exclusive abilities
- Physical-digital card synchronization

### Guardian Domain Slices

#### Slice G1: Guardian Profile Creation
**User Story**: As a family, we want to create Guardian profiles for each child.

**Implementation Stack**:
- **Mobile**: Profile creation form, avatar selection
- **Backend**: Guardian management API, profile validation
- **Database**: Guardians table, profiles table
- **Domain**: Age validation, profile customization

**Acceptance Criteria**:
- Each child has unique Guardian profile
- Age-appropriate customization options
- Profile linked to family Pacto

#### Slice G2: XP and Leveling System
**User Story**: As a Guardian, I want to gain experience and level up.

**Implementation Stack**:
- **Mobile**: XP progress display, level up animations
- **Backend**: XP calculation API, level progression
- **Database**: Guardian_xp table, level_rewards table
- **Domain**: XP sources, level benefits

**Acceptance Criteria**:
- XP earned from walks, battles, challenges
- Level progression from 1-10
- Unlock rewards at each level

#### Slice G3: Pacto Family Management
**User Story**: As a Guía del Pacto, I want to manage our family group.

**Implementation Stack**:
- **Mobile**: Pacto management UI, member invitation
- **Backend**: Pacto administration API, role management
- **Database**: Pactos table, pacto_members table
- **Domain**: Family group rules, role rotation

**Acceptance Criteria**:
- Create and manage family Pacto
- Invite 2-6 family members
- Rotate Guía del Pacto role

## Implementation Sequence

### Phase 1: Foundation (Weeks 1-2)
1. G1: Guardian Profile Creation
2. W1: Basic Step Tracking
3. C1: QR Code Card Collection

### Phase 2: Core Gameplay (Weeks 3-4)
1. B1: Basic Battle Mechanics
2. C2: Deck Management
3. G2: XP and Leveling System

### Phase 3: Family Features (Weeks 5-6)
1. G3: Pacto Family Management
2. W2: Magic Route Creation
3. B2: Daily Challenge System

### Phase 4: Advanced Features (Weeks 7-8)
1. W3: Route Completion Tracking
2. B3: Cooperative Battle Mode
3. C3: NFC Premium Cards

## Technical Implementation Guidelines

### Database Schema per Slice
Each slice includes its own database migration scripts with:
- Table definitions
- Initial data seeds
- Index optimizations
- Constraint definitions

### API Design per Slice
Each slice includes:
- RESTful endpoints
- Request/response DTOs
- Error handling
- API documentation

### Mobile UI per Slice
Each slice includes:
- Flutter screens/widgets
- BLoC state management
- Navigation integration
- Responsive design

### Testing Strategy per Slice
Each slice includes:
- Unit tests for domain logic
- Integration tests for API endpoints
- Widget tests for UI components
- End-to-end user journey tests

## Deployment Strategy

### Feature Toggles
Each slice can be enabled/disabled via feature toggles:
- Development testing
- Gradual rollout
- A/B testing capabilities

### Independent Deployment
Slices can be deployed independently:
- Backend API versioning
- Mobile app feature flags
- Database migration coordination

### Monitoring per Slice
Each slice includes:
- Performance metrics
- User adoption tracking
- Error monitoring
- Business value measurement

## Success Metrics

### User Engagement
- Daily active users per slice
- Feature adoption rates
- User retention impact

### Technical Quality
- Code coverage per slice
- Performance benchmarks
- Error rates
- API response times

### Business Value
- Feature usage analytics
- User feedback scores
- Development velocity
- Time to market

This vertical slicing strategy ensures that every feature delivered provides immediate value to users while maintaining technical excellence and enabling rapid iteration.