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

## FOR PROPER OSes: LINUX and MAC:

- Build and test commands (instead of `.\gradlew.bat`):

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

- Cyrillic characters in the path are not a problem, but an ASCII-only path is still safer. `android.overridePathCheck=true` in `gradle.properties` is only needed for Windows paths with Cyrillic characters.
- `gradlew` is already executable. If the permission is lost: `chmod +x gradlew`.
- `gradlew` must use LF line endings, otherwise you get `bad interpreter`. `.gitattributes` enforces this.
- JDK: Android Studio uses its bundled JBR, but `./gradlew` from a terminal needs a JDK on `PATH` or `JAVA_HOME`. Gradle downloads JDK 25 itself (see `gradle/gradle-daemon-jvm.properties`) if the network is available on the first build.
- Android SDK: Android Studio creates `local.properties`. The path is `~/Library/Android/sdk` on macOS and `~/Android/Sdk` on Linux. The file is not committed.
- Emulator: the host is reachable at `10.0.2.2`, same as on Windows. For a physical device, use the computer's LAN IP.
- Linux: hardware acceleration for the emulator requires KVM, and the user must be in the `kvm` group.
- System files such as `.DS_Store` and `*~` are already in `.gitignore`.

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
