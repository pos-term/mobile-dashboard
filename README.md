# mobile-dashboard

> **Язык / Language:** Русский | [English](i18n/en/README.md)

Android-приложение кассира (Java): просмотр статуса транзакций в реальном времени и истории операций через REST API payment-processor.

## Технологии

- Java и XML Views;
- Android SDK 37, minSdk 24;
- Gradle 9.6 и Android Gradle Plugin 9.4.1;
- AndroidX, Material 3 и ConstraintLayout;
- Retrofit 3, OkHttp 4 и Gson.

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

## ДЛЯ НОРМАЛЬНЫХ ОС LINUX и MAC:

- Команды сборки и тестов (вместо `.\gradlew.bat`):

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

- Кириллица в пути не мешает, но ASCII-путь всё равно безопаснее. Параметр `android.overridePathCheck=true` в `gradle.properties` нужен только для Windows-пути с кириллицей.
- `gradlew` уже исполняемый. Если права потерялись: `chmod +x gradlew`.
- Окончания строк `gradlew` должны быть LF, иначе будет `bad interpreter`. Это гарантирует `.gitattributes`.
- JDK: Android Studio использует встроенный JBR, но для `./gradlew` из терминала нужен JDK в `PATH` или `JAVA_HOME`. Gradle сам скачает JDK 25 (см. `gradle/gradle-daemon-jvm.properties`), если интернет доступен при первой сборке.
- Android SDK: `local.properties` создаёт Android Studio. На macOS путь `~/Library/Android/sdk`, на Linux `~/Android/Sdk`. Файл не коммитится.
- Эмулятор: хост доступен по `10.0.2.2`, как и на Windows. Для физического устройства используйте IP компьютера в локальной сети.
- Linux: для аппаратного ускорения эмулятора нужен KVM, а пользователь должен состоять в группе `kvm`.
- Системные файлы `.DS_Store`, `*~` и т.п. уже в `.gitignore`.

## REST API

`PaymentProcessorClient` реализует запросы истории транзакций, одной транзакции и списка терминалов. Адрес API и Bearer-токен передаются при создании клиента и не хранятся в репозитории:

```java
ApiConfiguration configuration = new ApiConfiguration(
        "http://10.0.2.2:8080/",
        token,
        true
);
PaymentProcessorClient client = PaymentProcessorClient.create(configuration);
PaymentProcessorApi api = client.getApi();
```

`10.0.2.2` — адрес компьютера-хоста из стандартного Android Emulator. Для физического устройства нужен доступный ему адрес backend. Незашифрованный HTTP разрешён только в debug-сборке; release-клиент должен использовать HTTPS. Третий аргумент включает базовые сетевые логи, при этом заголовок `Authorization` скрывается.

Unit-тесты API-клиента:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Структура приложения

```text
com.posterm.mobiledashboard
├── ui          # Activity, Fragment и ViewModel
├── model       # доменные модели
├── network     # REST- и SSE-клиенты
├── repository  # координация источников данных
└── data        # локальные источники и хранение данных
```

На текущем этапе готовы запускаемый каркас и протестированный REST-клиент. Подключение клиента к экрану и пользовательские сценарии будут добавляться отдельными feature-ветками.

## Контракты

- [REST API](https://github.com/pos-term/infra/blob/main/api/README.md): OpenAPI-спецификация [openapi.yaml](https://github.com/pos-term/infra/blob/main/api/openapi.yaml)
- [Kafka](https://github.com/pos-term/infra/blob/main/docs/kafka/README.md): JSON Schema в [infra/schemas](https://github.com/pos-term/infra/blob/main/schemas)

> Общая информация по проекту и участниках: https://github.com/pos-term/.github/blob/main/profile/README.md
