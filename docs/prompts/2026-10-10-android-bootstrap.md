# Android bootstrap prompt

- Date: 2026-10-10
- Branch: `chore/android-bootstrap`
- Scope: project bootstrap only

## Source request

> Перейди в нужную ветку и выполни второй пункт плана: создай Android-каркас на Java и XML, настрой Gradle, базовую тему и структуру приложения; каждое логическое изменение фиксируй отдельным коммитом.

## Implementation decisions

- Generated an Empty Views Activity project with Android Studio.
- Used Java for application code and Kotlin DSL only for Gradle configuration.
- Selected API 37, minSdk 24, and package `com.posterm.mobiledashboard`.
- Established `ui`, `model`, `network`, `repository`, and `data` package boundaries.
- Enabled AGP builds from the course workspace path with `android.overridePathCheck=true` because the Windows path contains Cyrillic characters.

## Verification

- `gradlew.bat assembleDebug` — successful.
- The generated APK is available under `app/build/outputs/apk/debug/`.
- Local unit-test execution should use an ASCII-only checkout path on Windows because the Gradle test worker cannot reliably load classes from the current Cyrillic workspace path.
