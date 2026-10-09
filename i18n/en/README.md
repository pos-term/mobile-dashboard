# mobile-dashboard

> **Language:** English | [Русский](../../README.md)

Cashier Android application (Java): real-time transaction status viewing and operation history via payment-processor REST API.

## Technology

- Java and XML Views;
- Android SDK 37 with minSdk 24;
- Gradle 9.6 and Android Gradle Plugin 9.4.1;
- AndroidX, Material 3, and ConstraintLayout.

## Run the app

1. Open the repository root in Android Studio.
2. Install Android SDK 37 if the IDE requests it.
3. Wait for Gradle Sync to finish.
4. Select an emulator or a connected Android device and run the `app` configuration.

Build the debug APK from PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`. On Windows, an ASCII-only repository path such as `C:\dev\mobile-dashboard` is recommended for the most reliable Gradle and unit-test execution.

## Application structure

```text
com.posterm.mobiledashboard
├── ui          # activities, fragments, and view models
├── model       # domain models
├── network     # REST and SSE clients
├── repository  # data-source coordination
└── data        # local data sources and persistence
```

The current milestone provides a minimal runnable screen. API integration and user flows will be added in separate feature branches.

## Contracts

- [REST API](https://github.com/pos-term/infra/blob/main/api/i18n/en/README.md): OpenAPI specification [openapi.yaml](https://github.com/pos-term/infra/blob/main/api/openapi.yaml)
- [Kafka](https://github.com/pos-term/infra/blob/main/docs/kafka/i18n/en/README.md): JSON Schema in [infra/schemas](https://github.com/pos-term/infra/blob/main/schemas)

> General project information and participants: https://github.com/pos-term/.github/blob/main/i18n/README_EN.md
