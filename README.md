# mobile-dashboard

> **Язык / Language:** Русский | [English](i18n/en/README.md)

Android-приложение кассира (Java): просмотр статуса транзакций в реальном времени и истории операций через REST API payment-processor.

## Технологии

- Java и XML Views;
- Android SDK 37, minSdk 24;
- Gradle 9.6 и Android Gradle Plugin 9.4.1;
- AndroidX, Material 3 и ConstraintLayout.

## Запуск

1. Откройте корень репозитория в Android Studio.
2. Установите Android SDK 37, если IDE предложит это сделать.
3. Дождитесь завершения Gradle Sync.
4. Выберите эмулятор или подключённое Android-устройство и запустите конфигурацию `app`.

Сборка debug APK из PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

APK появится в `app/build/outputs/apk/debug/`. На Windows для наиболее стабильной работы Gradle и unit-тестов рекомендуется путь к репозиторию, содержащий только ASCII-символы, например `C:\dev\mobile-dashboard`.

## Структура приложения

```text
com.posterm.mobiledashboard
├── ui          # Activity, Fragment и ViewModel
├── model       # доменные модели
├── network     # REST- и SSE-клиенты
├── repository  # координация источников данных
└── data        # локальные источники и хранение данных
```

На текущем этапе готов минимальный запускаемый экран. Клиент API и пользовательские сценарии будут добавляться отдельными feature-ветками.

## Контракты

- [REST API](https://github.com/pos-term/infra/blob/main/api/README.md): OpenAPI-спецификация [openapi.yaml](https://github.com/pos-term/infra/blob/main/api/openapi.yaml)
- [Kafka](https://github.com/pos-term/infra/blob/main/docs/kafka/README.md): JSON Schema в [infra/schemas](https://github.com/pos-term/infra/blob/main/schemas)

> Общая информация по проекту и участниках: https://github.com/pos-term/.github/blob/main/profile/README.md
