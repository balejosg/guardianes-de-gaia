# 📱 Guardianes Mobile

Flutter mobile application for the Guardianes de Gaia project - a cooperative card game that gamifies walking to school for families.

## 🏗️ Architecture

- **Pattern**: Clean Architecture with BLoC state management
- **State Management**: flutter_bloc
- **DI**: get_it + injectable

### Directory Structure

```
lib/
├── core/              # Shared utilities, constants, themes
├── features/          # Feature modules (vertical slices)
│   ├── auth/          # Authentication (login, register)
│   ├── cards/         # Card collection & scanning
│   ├── home/          # Home screen
│   └── walking/       # Step tracking
├── shared/            # Shared components
└── main.dart          # App entry point
```

### Feature Module Structure

Each feature follows the Clean Architecture pattern:

```
feature/
├── data/
│   ├── datasources/   # Remote and local data sources
│   ├── models/        # Data transfer objects
│   └── repositories/  # Repository implementations
├── domain/
│   ├── entities/      # Business entities
│   ├── repositories/  # Repository interfaces
│   └── usecases/      # Business logic
└── presentation/
    ├── bloc/          # BLoC classes (events, states, bloc)
    ├── pages/         # Full screen widgets
    └── widgets/       # Reusable UI components
```

## 🚀 Getting Started

### Prerequisites

- Flutter 3.3+ (SDK >=3.0.0 <4.0.0)
- Android Studio / Xcode
- Backend services running (see root README)

### Setup

```bash
# Navigate to mobile directory
cd mobile/guardianes_mobile

# Get dependencies
flutter pub get

# Generate code (injectable, json_serializable)
flutter pub run build_runner build --delete-conflicting-outputs

# Run the app
flutter run
```

### Environment Configuration

The app connects to different backend environments:

| Environment | API URL |
|-------------|---------|
| Development | `http://dev-guardianes.duckdns.org` |
| Staging | `http://stg-guardianes.duckdns.org` |
| Production | `https://guardianes.duckdns.org` |

## 🧪 Testing

```bash
# Run all tests
flutter test

# Run tests with coverage
flutter test --coverage

# Run integration tests (requires emulator)
flutter test integration_test/
```

## 📦 Build

```bash
# Build APK (Android)
flutter build apk --release

# Build App Bundle (Android - for Play Store)
flutter build appbundle

# Build iOS (requires macOS)
flutter build ios --release
```

## 🔧 Key Dependencies

| Package | Purpose |
|---------|---------|
| `flutter_bloc` | State management |
| `equatable` | Value equality |
| `get_it` + `injectable` | Dependency injection |
| `http` | HTTP client |
| `shared_preferences` | Local storage |
| `mobile_scanner` | QR code scanning |
| `pedometer` | Step counting |
| `permission_handler` | Runtime permissions |

## 📚 Related Documentation

- [Project Overview](../../docs/PROYECTO.md)
- [Tech Stack](../../docs/TECH_STACK.md)
- [Ubiquitous Language](../../docs/UBIQUITOUS_LANGUAGE.md)
- [Vertical Slicing Strategy](../../docs/VERTICAL_SLICING_STRATEGY.md)
