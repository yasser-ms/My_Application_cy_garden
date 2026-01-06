# CY Garden App - Autonomous Garden Management System

An Android application for intelligent garden management at CY Cergy Paris University. This system enables students to monitor, manage, and document their assigned garden parcels through real-time sensor data, automated alerts, and comprehensive cultivation tracking.

**Version:** 1.1.0  
**Target SDK:** Android 34 (API 34)  
**Minimum SDK:** Android 24 (API 24)

---

## Table of Contents

- [Overview](#overview)
- [Core Features](#core-features)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
- [Database Schema](#database-schema)
- [Key Components](#key-components)
- [Security Implementation](#security-implementation)
- [Offline Capabilities](#offline-capabilities)
- [Future Enhancements](#future-enhancements)

---

## Overview

The CY Garden App addresses the challenge of managing autonomous garden plots in an educational environment. Students cultivate assigned parcels while the system provides continuous environmental monitoring, cultivation guidance, and historical documentation. The application integrates hardware sensors (Currently data is mocked via a simple API), GPS localization, and cloud authentication to deliver a complete garden management solution.

### Problem Statement

Traditional garden management relies on manual monitoring and record-keeping, which can lead to crop failures from missed irrigation, improper environmental conditions, or inadequate documentation. This application automates monitoring while empowering students with data-driven insights and comprehensive cultivation histories.

### Solution Approach

The system employs a hybrid architecture combining Firebase for authentication and real-time data synchronization with SQLite for offline-first data persistence. Sensor readings are cached locally and synchronized when connectivity permits, ensuring continuous operation regardless of network availability.

---

## Core Features

### 1. Authentication & User Management
- Firebase Authentication with email/password
- CY University email domain validation (`@cyu.fr`)
- Session persistence with SharedPreferences
- Automatic user profile creation in Firebase Realtime Database

### 2. Real-Time Sensor Monitoring
- Four-parameter environmental tracking:
  - Soil humidity (percentage)
  - Ambient temperature (Celsius)
  - Light intensity (lux)
  - Soil pH level
- Historical data visualization with 7-day and 30-day views
- Interactive charts using MPAndroidChart library
- Critical threshold alerts with color-coded indicators

### 3. Parcel Management
- Visual garden map with interactive parcel selection
- Parcel reservation system with cultivation planning
- Occupancy status tracking
- Multi-parcel support per user account

### 4. GPS-Based Localization
- Real-time user position tracking
- Distance calculation to garden center
- Proximity-based parcel sorting
- Turn-by-turn navigation integration with Google Maps
- Directional indicators to assigned parcels

### 5. Photo Documentation
- Camera2 API integration for high-quality image capture
- Metadata tagging (parcel ID, timestamp, notes)
- Photo gallery with chronological sorting
- Full-screen image viewer with deletion capability

### 6. Cultivation Journal
- Activity logging (observation, watering, fertilization, pruning, harvest)
- Water quantity tracking for irrigation events
- Chronological entry display
- Long-press deletion with confirmation

### 7. Plant Library
- Comprehensive plant database with botanical information
- Search functionality by common or scientific name
- Detailed cultivation requirements:
  - Planting and harvest periods
  - Watering frequency
  - Sunlight requirements
  - Soil type preferences
  - Companion planting compatibility
  - Growth duration estimates

### 8. Smart Notifications
- Automated alerts for critical environmental conditions
- Background monitoring via WorkManager
- Notification history with severity indicators
- Dismissible alert system

### 9. Offline Mode
- Complete offline functionality with SQLite caching
- Visual network status indicators
- Automatic synchronization on connectivity restore
- Data persistence across app restarts

---

## System Architecture

### Design Pattern: Repository Pattern

The application implements the Repository pattern to abstract data sources and provide a single source of truth for domain data. This architecture separates business logic from data access implementation.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│          (Activities, Adapters, UI Components)          │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Business Logic Layer                 │
│             (GardenRepository, NetworkStateMonitor)     │
└─────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────┬──────────────────────────────────┐
│             Local Data Source            │      Remote Data Source          │
│  (SQLite/DatabaseHelper, MockApiService) │          (Firebase)              │
└──────────────────────────────────────────┴──────────────────────────────────┘
```

### Component Breakdown

**Presentation Layer**
- Activities handle user interactions and lifecycle management
- RecyclerView adapters manage list presentations
- Custom views (GardenMapView) for specialized visualizations

**Business Logic Layer**
- `GardenRepository`: Coordinates data operations between local and remote sources
- `NetworkStateMonitor`: Tracks connectivity and manages offline transitions
- `NotificationHelper`: Generates and manages system notifications

**Data Layer**
- `DatabaseHelper`: SQLite operations with schema versioning
- `FirebaseAuthManager`: Authentication flow management
- `MockApiService`: Simulates REST API for sensor data

---

## Technology Stack

### Core Android Components
- **Language:** Java 8+
- **Build System:** Gradle with Kotlin DSL
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34 (Android 14)

### Major Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Firebase BOM | 34.6.0 | Authentication, Realtime Database, Storage |
| WorkManager | 2.9.0 | Background task scheduling |
| MPAndroidChart | 3.1.0 | Data visualization |
| CameraX/Camera2 | 1.3.1 | Image capture |
| Play Services Location | 21.0.1 | GPS functionality |
| Play Services Maps | 18.2.0 | Map integration |
| Gson | 2.10.1 | JSON serialization |
| Material Components | 1.9.0 | UI design system |

### Architecture Components
- **LiveData:** Reactive data observation
- **ViewModel:** UI-related data management

---

## Database Schema

### SQLite Tables

#### sensor_readings
Stores environmental monitoring data.

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment |
| parcel_id | TEXT | Reference to parcel |
| humidity | INTEGER | Soil moisture (0-100%) |
| temperature | REAL | Celsius |
| light_level | INTEGER | Lux |
| ph | REAL | Soil pH (0-14) |
| timestamp | INTEGER | Unix epoch milliseconds |

#### parcels
Garden plot assignments.

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment |
| parcel_number | TEXT UNIQUE | e.g., "B4" |
| owner_email | TEXT | User assignment |
| plant_type | TEXT | Crop variety |
| planting_date | TEXT | ISO date |
| harvest_date | TEXT | Estimated harvest |
| is_occupied | INTEGER | Boolean (0/1) |

#### photos
Image documentation metadata.

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT PRIMARY KEY | Composite: timestamp_parcelId |
| parcel_id | TEXT | Associated parcel |
| user_email | TEXT | Photographer |
| file_path | TEXT | Absolute path |
| notes | TEXT | Optional description |
| timestamp | INTEGER | Capture time |

#### journal_entries
Cultivation activity log.

| Column | Type | Description |
|--------|------|-------------|
| id | TEXT PRIMARY KEY | UUID |
| parcel_id | TEXT | Associated parcel |
| user_email | TEXT | Author |
| entry_type | TEXT | Activity category |
| notes | TEXT | Description |
| water_amount | REAL | Liters (watering only) |
| timestamp | INTEGER | Entry time |

#### plants
Botanical reference database.

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment |
| name | TEXT | Common name |
| scientific_name | TEXT | Latin binomial |
| category | TEXT | Classification |
| planting_period | TEXT | Seasonal window |
| harvest_period | TEXT | Maturity window |
| care_instructions | TEXT | Cultivation notes |
| watering_frequency | TEXT | Schedule |
| sunlight_requirement | TEXT | Exposure needs |
| soil_type | TEXT | Substrate preference |
| compatibility | TEXT | Companion plants |
| growth_duration_days | INTEGER | Days to maturity |

#### notification_history
Alert tracking.

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment |
| parcel_id | TEXT | Source parcel |
| alert_type | TEXT | Trigger condition |
| message | TEXT | Alert content |
| severity | TEXT | critical/warning/info |
| is_read | INTEGER | Boolean |
| timestamp | INTEGER | Alert time |

---

## Security Implementation

### Authentication
- Firebase Authentication with email/password
- No passwords stored locally
- Session tokens managed by Firebase SDK
- Domain validation enforces `@cyu.fr` emails only

### Data Protection
- SQLite database file permissions restricted to app
- External storage (photos) isolated to app-specific directory
- No sensitive data logged to Logcat in production builds

### Input Validation
```java
private boolean isValidEmail(String email) {
    return email.endsWith("@cyu.fr") && email.length() > 10;
}

private boolean isValidPassword(String password) {
    return password.length() >= 6;
}
```

---

## Offline Capabilities

The application operates fully offline after initial data load:

### Offline-First Design
1. **SQLite as Primary Cache**
   - All sensor readings stored locally
   - Parcel data persisted
   - Photos saved to device storage

2. **Network State Monitoring**
   ```java
   NetworkStateMonitor.getInstance(context).addListener((state, isConnected) -> {
       if (isConnected) {
           syncPendingData();
       } else {
           showOfflineBanner();
       }
   });
   ```

3. **Automatic Synchronization**
   - Queued operations executed on connection restore
   - Conflict resolution: last-write-wins
   - Background sync via WorkManager

### User Experience
- Visual offline indicator banner
- Cached data displayed immediately
- Sync progress notifications
- No functionality degradation in offline mode
