# mobile-dashboard

> **Language:** English | [Русский](../../README.md)

Cashier Android application (Java) for viewing transaction history and statuses via payment-processor REST API 1.1.0.

## Technology

- Java and XML Views;
- Android SDK 37 with minSdk 24;
- Gradle 9.6 and Android Gradle Plugin 9.4.1;
- AndroidX Lifecycle, RecyclerView, SwipeRefreshLayout, Material 3, and ConstraintLayout;
- Retrofit 3, OkHttp 4, and Gson.

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

Run the API client and ViewModel unit tests with:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Local debug configuration

Start the Compose stack from the `mobile-dashboard` root:

```sh
docker compose -f ../infra/docker-compose.yml up -d --build payment-processor
```

Add the following settings to the root `local.properties`, keeping the existing `sdk.dir`:

```properties
payment.api.url=http://10.0.2.2:8080/
payment.api.token=dev-token
```

The standard Android Emulator uses `10.0.2.2` to reach the host. On a physical device, use a reachable host address. The token must match the Compose `API_TOKEN`. Its default test value is `dev-token`.

Rebuild and reinstall the debug APK after changing these settings. Git ignores `local.properties`. The settings are embedded in the debug APK, so use a local test token. Cleartext HTTP is allowed only in debug builds. The history screen shows a configuration error if the URL or token is missing.

Set `payment.demo=true` and rebuild the debug APK to explicitly use mock data without a server. Remove this setting or set it to `false` to return to REST.

## Transaction history

The screen loads history through `GET /v1/transactions` and supports:

- initial loading and pull-to-refresh;
- cursor pagination with 20 transactions per page and previous- and next-page controls;
- filtering by terminal, one or more statuses, ID prefix, amount range, currency, failure reason code, and date range; currency and reason are selected from lists, and entered amounts are converted to API minor units automatically;
- empty and error states with retry;
- transaction IDs, `pending`, `success`, and `failed` statuses, and decline reason codes when available;
- minor-unit amounts formatted with each currency's fraction digits.

Demo mode uses the fixtures in `app/src/main/assets/transactions/`. Terminal ID filtering is case-sensitive in both modes.

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
├── network     # REST client
├── repository  # data-source coordination
└── data        # local data sources and persistence
```

## Contracts

- [REST API](https://github.com/pos-term/infra/blob/main/api/i18n/en/README.md): OpenAPI specification [openapi.yaml](https://github.com/pos-term/infra/blob/main/api/openapi.yaml)
- [Kafka](https://github.com/pos-term/infra/blob/main/docs/kafka/i18n/en/README.md): JSON Schema in [infra/schemas](https://github.com/pos-term/infra/blob/main/schemas)

> General project information and participants: https://github.com/pos-term/.github/blob/main/i18n/README_EN.md
