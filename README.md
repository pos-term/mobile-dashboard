# mobile-dashboard

> **Язык / Language:** Русский | [English](i18n/en/README.md)

Android-приложение кассира (Java) для просмотра истории и статусов операций через REST API payment-processor 1.1.0.

## Технологии

- Java и XML Views;
- Android SDK 37, minSdk 24;
- Gradle 9.6 и Android Gradle Plugin 9.4.1;
- AndroidX Lifecycle, RecyclerView, SwipeRefreshLayout, Material 3 и ConstraintLayout;
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

## Локальная debug-конфигурация REST API

Из корня `mobile-dashboard` запустите локальный Compose-стенд:

```sh
docker compose -f ../infra/docker-compose.yml up -d --build payment-processor
```

Добавьте параметры в корневой `local.properties`, сохранив существующий `sdk.dir`:

```properties
payment.api.url=http://10.0.2.2:8080/
payment.api.token=dev-token
```

`10.0.2.2` — адрес хоста из стандартного Android Emulator. Для физического устройства укажите доступный ему адрес. Значение `payment.api.token` должно совпадать с `API_TOKEN` стенда. `dev-token` — стандартное тестовое значение Compose.

После изменения файла пересоберите debug APK и установите его заново. `local.properties` исключён из Git. Параметры встраиваются только в debug APK, поэтому используйте тестовый токен, рассчитанный на локальную разработку. HTTP разрешён только для debug. Если адрес или токен не настроены, экран истории покажет ошибку конфигурации.

Чтобы явно включить историю на мок-данных без сервера, добавьте `payment.demo=true` и пересоберите debug APK. Для возврата к REST удалите этот параметр или задайте `false`.

Unit-тесты API-клиента:

```powershell
.\gradlew.bat testDebugUnitTest
```

## История транзакций

Экран загружает операции через `GET /v1/transactions` и поддерживает:

- начальную загрузку и pull-to-refresh;
- курсорную пагинацию по 20 операций с кнопками перехода на предыдущую и следующую страницу;
- фильтрацию по терминалу, одному или нескольким статусам, префиксу ID, диапазону суммы, валюте, коду причины отказа и диапазону дат; валюта и причина выбираются из списка, а введённые суммы автоматически переводятся в минорные единицы API;
- пустое состояние и ошибку с повтором запроса;
- ID транзакции, статусы `pending`, `success` и `failed`, а также код причины отказа при наличии;
- суммы в минорных единицах с учётом числа знаков валюты.

Деморежим использует файлы из `app/src/main/assets/transactions/`. В обоих режимах
`terminal_id` фильтруется с учётом регистра.

## Структура приложения

```text
com.posterm.mobiledashboard
├── ui          # Activity, Fragment и ViewModel
├── model       # доменные модели
├── network     # REST-клиент
├── repository  # координация источников данных
└── data        # локальные источники и хранение данных
```

## Контракты

- [REST API](https://github.com/pos-term/infra/blob/main/api/README.md): OpenAPI-спецификация [openapi.yaml](https://github.com/pos-term/infra/blob/main/api/openapi.yaml)
- [Kafka](https://github.com/pos-term/infra/blob/main/docs/kafka/README.md): JSON Schema в [infra/schemas](https://github.com/pos-term/infra/blob/main/schemas)

> Общая информация по проекту и участниках: https://github.com/pos-term/.github/blob/main/profile/README.md
